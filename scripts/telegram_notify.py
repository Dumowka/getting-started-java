import glob
import json
import os
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

API_URL = "https://api.telegram.org/bot{token}/sendMessage"
MAX_MESSAGE_LENGTH = 4096
TIMEOUT_SECONDS = 15
SECRET_NAMES = ("TELEGRAM_BOT_TOKEN", "TELEGRAM_CHAT_ID")
UNREADABLE = "unreadable"


def read_junit_results(results_dir):
    files = sorted(glob.glob(os.path.join(results_dir, "TEST-*.xml"))) if results_dir else []
    if not files:
        return None
    stats = {"total": 0, "passed": 0, "failed": 0, "skipped": 0, "errors": 0}
    declared = {"tests": 0, "failures": 0, "skipped": 0, "errors": 0}
    for path in files:
        root = ET.parse(path).getroot()
        suites = [root] if root.tag == "testsuite" else root.iter("testsuite")
        for suite in suites:
            for key in declared:
                declared[key] += int(suite.get(key, "0"))
            for case in suite.findall("testcase"):
                stats["total"] += 1
                if case.find("failure") is not None:
                    stats["failed"] += 1
                elif case.find("error") is not None:
                    stats["errors"] += 1
                elif case.find("skipped") is not None:
                    stats["skipped"] += 1
                else:
                    stats["passed"] += 1
    counted = {
        "tests": stats["total"],
        "failures": stats["failed"],
        "skipped": stats["skipped"],
        "errors": stats["errors"],
    }
    if counted != declared:
        print(f"::warning::JUnit XML suite counters {declared} differ from testcase elements {counted}")
    return stats


def determine_status(test_job_result, compile_outcome, tests_outcome, stats):
    if test_job_result == "cancelled":
        return "⚪", "CANCELLED"
    if tests_outcome not in ("success", "failure"):
        if compile_outcome == "failure":
            return "❌", "BUILD FAILED: compilation, tests not run"
        return "❌", "BUILD FAILED before tests"
    if stats is None:
        if tests_outcome == "failure":
            return "❌", "TESTS FAILED: no JUnit XML"
        return "⚠️", "NO TESTS EXECUTED"
    if stats == UNREADABLE:
        return "❌", "TEST RESULTS UNREADABLE"
    if stats["failed"] + stats["errors"] > 0:
        return "❌", "TESTS FAILED"
    if tests_outcome == "failure":
        return "❌", "FAILED: Gradle test task failed"
    if stats["total"] == 0:
        return "⚠️", "NO TESTS EXECUTED"
    if test_job_result != "success":
        return "❌", "CI FAILED after tests"
    return "✅", "PASSED"


def format_tests_line(tests_outcome, stats):
    if tests_outcome not in ("success", "failure"):
        return "Tests: not run"
    if stats is None:
        return "Tests: no JUnit XML results"
    if stats == UNREADABLE:
        return "Tests: JUnit XML could not be read"
    return (
        f"Tests: total {stats['total']}, passed {stats['passed']}, failed {stats['failed']}, "
        f"skipped {stats['skipped']}, errors {stats['errors']}"
    )


def build_message(env, stats):
    icon, title = determine_status(
        env.get("NOTIFY_TEST_JOB_RESULT", ""),
        env.get("NOTIFY_COMPILE_OUTCOME", ""),
        env.get("NOTIFY_TESTS_OUTCOME", ""),
        stats,
    )
    ci = env.get("NOTIFY_CI", "")
    event = env.get("NOTIFY_EVENT", "")
    lines = [
        f"{icon} UI tests: {title}",
        f"Project: {env.get('NOTIFY_PROJECT', '')}",
        f"CI: {ci} ({event})" if event else f"CI: {ci}",
        f"Branch: {env.get('NOTIFY_BRANCH', '')}",
        f"Commit: {env.get('NOTIFY_COMMIT', '')[:7]}",
        format_tests_line(env.get("NOTIFY_TESTS_OUTCOME", ""), stats),
        f"Run: {env.get('NOTIFY_RUN_URL', '')}",
    ]
    report_url = env.get("NOTIFY_REPORT_URL", "")
    if report_url:
        lines.append(f"Report: {report_url}")
    return "\n".join(lines)[:MAX_MESSAGE_LENGTH]


def single_line(value):
    return " ".join(str(value).split())


def send_message(token, chat_id, text):
    payload = json.dumps(
        {"chat_id": chat_id, "text": text, "link_preview_options": {"is_disabled": True}}
    ).encode("utf-8")
    request = urllib.request.Request(
        API_URL.format(token=token),
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as response:
            status = response.status
            body = response.read()
    except urllib.error.HTTPError as error:
        status = error.code
        body = error.read()
    except OSError as error:
        print(f"::error::Telegram API request failed: {type(error).__name__}")
        return False
    try:
        data = json.loads(body)
    except ValueError:
        data = None
    if not isinstance(data, dict):
        print(f"::error::Telegram API returned HTTP {status} with a non-JSON response")
        return False
    ok = data.get("ok") is True
    print(f"Telegram API: HTTP {status}, ok={str(ok).lower()}")
    if status == 200 and ok:
        result = data.get("result")
        message_id = result.get("message_id") if isinstance(result, dict) else None
        print(f"Telegram message delivered, message_id={message_id}")
        return True
    print(
        f"::error::Telegram notification failed: HTTP {status}, ok={str(ok).lower()}, "
        f"error_code={single_line(data.get('error_code'))}, description={single_line(data.get('description'))}"
    )
    return False


def main(argv):
    results_dir = argv[1] if len(argv) > 1 else ""
    try:
        stats = read_junit_results(results_dir)
    except (ET.ParseError, ValueError, OSError) as error:
        print(f"::warning::Cannot read JUnit XML: {type(error).__name__}: {single_line(error)}")
        stats = UNREADABLE
    text = build_message(os.environ, stats)
    print("Telegram message:")
    print(text)
    missing = [name for name in SECRET_NAMES if not os.environ.get(name)]
    if missing:
        print(f"::warning::Telegram notification skipped: {', '.join(missing)} is not set")
        return 0
    delivered = send_message(os.environ["TELEGRAM_BOT_TOKEN"], os.environ["TELEGRAM_CHAT_ID"], text)
    return 0 if delivered else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
