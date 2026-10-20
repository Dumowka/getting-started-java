# План развития учебного проекта UI-автотестов

## Назначение

Постепенно добавить TestBase, независимые сценарии, Page Object, Allure-отчёты, GitHub Actions, Telegram и Jenkins, сохраняя существующий Java-стек.

В этом документе хранятся согласованные решения, статусы фаз, результаты проверок, ограничения и открытые вопросы. Общие правила работы агента находятся отдельно в [AGENTS.md](../../AGENTS.md). После каждой фазы обновлять этот план; не отмечать выполнение без подтверждённых результатов.

## Согласованные решения

Решения приняты пользователем 2026-10-20 по итогам фазы 0; подробности — в разделе фазы 0. Решения следующих фаз добавляются сюда только после их согласования.

- Java 21 через Gradle Java toolchain.
- Gradle Wrapper: актуальная стабильная версия на момент фазы 1, дистрибутив `bin`, без `distributionSha256Sum`.
- Зависимости: Selenide 7.18.2 и JUnit Jupiter 6.1.3, как в исходном `build.gradle`. Версии зависимостей без согласования не менять.
- Параметры запуска — штатные свойства Selenide: `selenide.baseUrl`, `selenide.browser`, `selenide.browserSize`, `selenide.headless`. Defaults задаются в проекте явно: baseUrl=https://github.com, browser=chrome, browserSize=1366x768, headless=false.
- Из Gradle в JVM тестов передаются только эти четыре свойства. В CI явно передавать `selenide.headless=true`.
- Page Object: fluent API; проверки — в методах страниц; элемент выносится в поле `private final SelenideElement`, если используется больше одного раза, иначе `$()` внутри метода; пакеты `tests` и `pages`; компоненты и BasePage — сразу.
- Allure (фаза 4, согласовано 2026-10-20): генератор — Allure Report 3 (npm-пакет `allure` 3.21.0) как devDependency проекта с точной версией и `package-lock.json`, `node_modules/` в `.gitignore`; запуск через `npx allure`. JVM-адаптеры — Allure Java 3.0.0 через `allure-bom`: `allure-jupiter` и `allure-selenide`. AspectJ weaver 1.9.25.1 как `-javaagent` задачи `test`.
- Шаги Allure: `@Step` с английскими названиями и параметрами только на публичных методах `SearchPage`; технические шаги Selenide — через `AllureSelenide` (`includeSelenideSteps(true)`), при падении — вложения Screenshot и Page source; listener регистрируется в `@BeforeAll` в `TestBase`.
- Результаты Allure — `build/allure-results` (задаётся в `src/test/resources/allure.properties`), очищаются в `doFirst` задачи `test`; отчёт — `build/allure-report`, генерируется с `--clean`.
- GitHub Actions (фаза 5, согласовано 2026-10-20): один workflow `.github/workflows/ui-tests.yml`. Триггеры — `workflow_dispatch` и `push` во все ветки с `paths-ignore` `docs/**` и `**/*.md`; без `pull_request` и `schedule` (риск HTTP 429 от github.com). Runner `ubuntu-24.04`, Chrome и ChromeDriver — предустановленные в образе; Chrome for Testing не ставится, `--no-sandbox` не используется.
- В CI передаются только `-Dselenide.headless=true` и `-Dselenide.browserSize` из input `browser_size` (`choice`: `1366x768` по умолчанию, `1920x1080`), через `env` с проверкой `case`; `baseUrl` и `browser` — defaults TestBase.
- В CI: Temurin `21` (`actions/setup-java`), Gradle через wrapper с `setup-gradle` (`cache-provider: basic`, проверка wrapper), Node.js 24.22.0 с `npm ci` и кэшем npm. Actions закреплены по полному SHA с тегом в комментарии; `permissions: {}` на уровне workflow, минимальные — на уровне job; secrets не нужны.
- Артефакты (14 дней): `allure-results`, `test-reports` (JUnit XML и HTML-отчёт Gradle), `allure-report`. Отчёт генерируется только при наличии `build/allure-results/*-result.json`. Публикация Allure-отчёта в GitHub Pages — только с `master` (в том числе при упавших тестах); Pages включается после слияния.

## Текущее состояние

- Фаза 0 завершена 2026-10-20: проверки выполнены, baseline 1/1 получен (Gradle 9.8.1 из локального кэша, Java 21), решения по параметрам, Java, wrapper и Page Object согласованы. Детали и ограничения — в разделе фазы 0.
- Фаза 1 реализована 2026-10-20 в ветке `phase-1` (один коммит; PR #2 влит в `master`): Gradle Wrapper 9.8.2, Java 21 toolchain, TestBase, передача четырёх свойств `selenide.*`; финальные запуски 1/1 с defaults и с переопределением. Детали — в разделе фазы 1.
- Фаза 2 реализована 2026-10-20 в ветке `phase-2` (коммит фазы и коммит с обновлением плана; по решению пользователя запушена в `origin`; PR #3 влит в `master`): в SearchTests добавлены `repoQualifierSearchTest` и `searchWithoutResultsTest`; весь набор 3/3 с defaults и в headless. Детали — в разделе фазы 2.
- Фаза 3 реализована 2026-10-20 в ветке `phase-3` (коммит фазы и коммит с расследованием длительности тестов; ветка запушена в `origin`; PR #4 влит в `master`): тесты и TestBase перенесены в пакет `tests`, Page Object (`BasePage`, `SearchPage`, три компонента) — в пакете `pages`; каждый тест отдельно 1/1, весь набор 3/3 с defaults и в headless. Детали — в разделе фазы 3.
- Фаза 4 реализована 2026-10-20 в ветке `phase-4` (один коммит; ветка запушена в `origin`; PR #5 влит в `master`): Allure Report 3.21.0 (npm), Allure Java 3.0.0 (`allure-jupiter`, `allure-selenide`), AspectJ 1.9.25.1, `@Step` на `SearchPage`, вложения при падении; весь набор 3/3 с defaults и в headless, контролируемое падение проверено. Детали — в разделе фазы 4.
- Фаза 5 реализована 2026-10-20 в ветке `phase-5` (запушена в `origin`, в `master` не влита): workflow GitHub Actions `ui-tests.yml`; в реальном GitHub Actions проверены успешный запуск 3/3, контролируемое падение assertion, ошибка компиляции и отсутствие результатов Allure. Ручной запуск с inputs и публикация в Pages не проверены (возможны только после слияния). Детали — в разделе фазы 5.
- Фазы 6–9 не реализованы.

Порядок дальнейшей работы: фазы выполняются по одной — согласование, реализация, проверка, отдельный коммит. Следующая — фаза 6, после подтверждения пользователя.

## Фаза 0 — Baseline

Статус: завершена 2026-10-20. Baseline получен разовым запуском через локально закэшированный Gradle 9.8.1 на Java 21: BUILD SUCCESSFUL, 1/1. Решения по вопросам согласования записаны ниже. Код, build.gradle и настройки не менялись.

### Репозиторий и git

- Корень Git-репозитория — `getting-started-java/` внутри рабочего каталога `/Users/dmitrii/Projects/getting-started-java ` (имя каталога заканчивается пробелом).
- Текущая ветка `phase-0` указывает на тот же коммит, что `master` и `origin/master`: `09be4d9` «Remove build artifacts from git and add .gitignore». Remote один: `origin` → https://github.com/Dumowka/getting-started-java.
- Tracked-файлы: `.gitignore`, `build.gradle`, `src/test/java/SearchTests.java`, изменений в них нет. Untracked: `AGENT.md` (пустой, 0 байт) и `docs/`.
- `.gitignore`: `.gradle/`, `build/`.
- В проекте нет `settings.gradle`, `gradle.properties`, `src/main` и `src/test/resources`.
- План ссылается на `AGENTS.md`, а в репозитории был только пустой `AGENT.md`. По решению пользователя 2026-10-20 он переименован в `AGENTS.md`; файл пока пустой и не отслеживается git.

### Сборка и тест

- `build.gradle`: плагин `java-library`, репозиторий `mavenCentral()`, `testImplementation` `com.codeborne:selenide:7.18.2` и `org.junit.jupiter:junit-jupiter:6.1.3`, `testRuntimeOnly` `org.junit.platform:junit-platform-launcher` без версии, кодировка UTF-8, `useJUnitPlatform()`. Toolchain, `sourceCompatibility` и передачи system properties в JVM тестов нет.
- `SearchTests` лежит в default package и содержит один тест `successfulSearchTest`. Он открывает абсолютный `https://github.com/search`, вводит `qa.guru` в `[aria-label='Search GitHub']` и проверяет, что `[data-testid='results-list']` содержит текст `QA.GURU`. Configuration Selenide не задаётся, TestBase нет, поэтому действуют встроенные defaults Selenide. Эти defaults в фазе не проверялись.

### Gradle Wrapper

- Отсутствует: нет `gradlew`, `gradlew.bat` и `gradle/wrapper/`. Не создавался.

### Версии и требования к Java

- Selenide 7.18.2 зависит от `selenium-java` 4.49.0 (POM `selenide` и `selenide-core` 7.18.2).
- JUnit Jupiter 6.1.3. `junit-platform-launcher` без версии выравнивается через `junit-bom` 6.1.3, который объявлен в Gradle Module Metadata JUnit. Это вывод из метаданных, а не результат разрешения этой сборки.
- Требования к Java: в Gradle Module Metadata у `selenide`, `selenide-core`, `junit-jupiter`, `junit-jupiter-api` и `junit-platform-launcher` указано `org.gradle.jvm.version = 17`. Байткод `selenide-core` и `junit-jupiter-api` имеет major 61 (Java 17), `selenium-api` 4.49.0 — major 55 (Java 11). Итог: нужна Java 17+.
- Источник версий — артефакты в локальном `~/.gradle/caches`, скачанные другими проектами. `gradle dependencies` не выполнялся, потому что нет Gradle.
- Локальная Java: `java -version` → OpenJDK 26.0.2.1. Это default, `JAVA_HOME` не задан. Также установлены Microsoft OpenJDK 21.0.12.1 и Amazon Corretto 17.0.20. В `~/.gradle/gradle.properties` вне проекта задано `org.gradle.java.installations.paths=/opt/homebrew/opt/openjdk@17`.
- Локальный Gradle: `gradle --version` → `command not found`. Gradle нет в PATH, в Homebrew и в SDKMAN. В `~/.gradle/wrapper/dists` лежат дистрибутивы других проектов: 8.9, 9.7.1 и 9.8.1. Они не использовались.
- Совместимость Gradle с локальной Java 26 не проверялась.

### Baseline-запуск

Команда из корня репозитория:

```bash
gradle test --tests SearchTests.successfulSearchTest --rerun-tasks --console=plain
```

Первая попытка 2026-10-20: `(eval):1: command not found: gradle`, exit code 127. Gradle не запустился, задача test не выполнялась, `build/` не создан.

Вторая попытка 2026-10-20 (решение пользователя — вариант Б): та же команда через `~/.gradle/wrapper/dists/gradle-9.8.1-bin/a2l6ltp3k1vth6lwipzm5p2jp/gradle-9.8.1/bin/gradle` с `JAVA_HOME` = Microsoft OpenJDK 21.0.12.1. Проект при этом не менялся.

- `gradle --version` этого дистрибутива: Gradle 9.8.1, Launcher JVM 21.0.12.1.
- Результат: BUILD SUCCESSFUL in 1m 42s, exit code 0. Задачи `compileTestJava` и `test` выполнены (2 actionable tasks: 2 executed).
- `build/test-results/test/TEST-SearchTests.xml`: `tests="1" skipped="0" failures="0" errors="0"`, `time="99.125"`.
- stderr теста: предупреждение SLF4J об отсутствии провайдера (NOP logger) и предупреждение Selenium о CDP: Chrome 155, ближайшая поддерживаемая версия — 153.
- Во время прогона Selenium Manager 0.4.49 распаковался в `~/.cache/selenium/manager` и скачал chromedriver 155.0.8059.84 в `~/.cache/selenium/chromedriver`. Это видно по временам изменения каталогов. Отсюда часть длительности теста; точная разбивка не измерялась.

### Ограничения

- Baseline получен Gradle-дистрибутивом другого проекта. Его zip-архив удалён, поэтому контрольную сумму дистрибутива проверить нельзя. Запуск с Java 26 по умолчанию не проверялся.
- Один успешный запуск не доказывает стабильность внешнего GitHub: возможны HTTP 429 и изменения разметки. Результаты по запросу `qa.guru` зависят от изменяемых данных.
- Версии зависимостей определены по метаданным артефактов, а не по разрешению графа этой сборкой (`gradle dependencies` не запускался).
- Временная диагностика не создавалась. В проекте изменён только этот раздел плана; `build/` и `.gradle/` после прогона исключены `.gitignore`.

### Решения пользователя (2026-10-20)

1. Baseline — разовый запуск через закэшированный Gradle без изменения проекта (вариант Б). Выполнен на Java 21: она выбрана для toolchain в пункте 3.
2. Параметры запуска: штатные имена свойств Selenide (`selenide.*`). Defaults задаются в проекте явно: `baseUrl=https://github.com`, `browser=chrome`, `browserSize=1366x768`, `headless=false`. Из Gradle в JVM тестов передаются только перечисленные свойства; в CI `headless=true`.
3. Java 21 через Gradle Java toolchain.
4. Gradle Wrapper нужен: актуальная стабильная версия на момент фазы 1, дистрибутив `bin`, без `distributionSha256Sum`.
5. Page Object: fluent API; проверки — в методах страниц; элемент выносится в поле `private final SelenideElement`, если используется больше одного раза, иначе `$()` внутри метода; пакеты `tests` и `pages`; компоненты и BasePage — сразу.

## Фаза 1 — TestBase

Статус: реализована 2026-10-20 в ветке `phase-1`, созданной от `master` после fast-forward до `3e7e2f8` (merge PR #1, фаза 0). Один коммит. По решению пользователя 2026-10-20 ветка запушена в `origin/phase-1` и для неё открыт PR #2 в `master`; PR #2 влит в `master`. Фаза 2 ожидает подтверждения пользователя.

Объём фазы:

- Добавить штатный Gradle Wrapper актуальной стабильной версии (`bin`, без `distributionSha256Sum`) с executable bit для gradlew.
- Зафиксировать Java 21 toolchain.
- Добавить TestBase: настройка Selenide в @BeforeEach, closeWebDriver в @AfterEach.
- Передавать в JVM тестов только четыре согласованных свойства `selenide.*` с явными defaults.
- Подключить TestBase к SearchTests и заменить абсолютный URL на /search.
- Проверить defaults, переопределение свойств и использование baseUrl внутри JVM тестов.
- Проверить отсутствие запуска браузера при настройке, изоляцию сессий и cleanup после успеха и контролируемого падения.
- Удалить временную диагностику, восстановить исходный assertion, повторить успешные запуски и создать отдельный коммит без push.

Финальные запуски:

```bash
./gradlew test --tests SearchTests.successfulSearchTest --rerun-tasks --console=plain
./gradlew test --tests SearchTests.successfulSearchTest -Dselenide.baseUrl=https://www.github.com -Dselenide.browser=chrome -Dselenide.browserSize=1920x1080 -Dselenide.headless=true --rerun-tasks --console=plain
```

### Изменения

- `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`: Gradle 9.8.2, `distributionUrl=…/gradle-9.8.2-bin.zip`, `distributionSha256Sum` отсутствует, у `gradlew` режим `100755`.
- `build.gradle`: `java.toolchain.languageVersion = 21`; в `test` в JVM тестов передаются только `selenide.baseUrl`, `selenide.browser`, `selenide.browserSize`, `selenide.headless` и только если они заданы при запуске Gradle (`providers.systemProperty`). Версии зависимостей не менялись.
- `src/test/java/TestBase.java` (default package, как `SearchTests`): `@BeforeEach` задаёт `Configuration.baseUrl/browser/browserSize/headless` из `System.getProperty` с явными defaults `https://github.com`, `chrome`, `1366x768`, `false`; `@AfterEach` вызывает `closeWebDriver()`. Defaults хранятся только в TestBase, поэтому действуют и при запуске вне Gradle. `headless` разбирается `Boolean.parseBoolean`: любое значение кроме `true` (без учёта регистра) даёт `false`, отдельной валидации нет.
- `SearchTests`: `extends TestBase`, `open("https://github.com/search")` → `open("/search")`. Локаторы, данные и assertion не менялись.
- По решению пользователя 2026-10-20 после первого коммита фазы: `.gitignore` дополнен `bin/` (`.class`-файлы, которые компилирует IDE VS Code Java); добавлен `.gitattributes`, как его генерирует `gradle init`: `/gradlew text eol=lf`, `*.bat text eol=crlf`, `*.jar binary`.

### Gradle Wrapper

- Актуальная стабильная версия по https://services.gradle.org/versions/current на 2026-10-20: 9.8.2 (`"current": true`, `"final": true`, сборка 2026-10-19). В кэше был только 9.8.1, поэтому скачивание 9.8.2 согласовано с пользователем.
- Генерация: закэшированный `~/.gradle/wrapper/dists/gradle-9.8.1-bin/…/bin/gradle wrapper --gradle-version 9.8.2 --distribution-type bin` с `JAVA_HOME` = OpenJDK 21.0.12.1 → BUILD SUCCESSFUL. Затем `./gradlew wrapper --gradle-version 9.8.2 --distribution-type bin` без `JAVA_HOME` (Java 26 по умолчанию): скачан `gradle-9.8.2-bin.zip`, BUILD SUCCESSFUL in 19m (почти всё время — загрузка дистрибутива).
- sha256 `gradle-wrapper.jar` после обоих запусков — `3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98`, совпадает с `wrapperChecksum` версии 9.8.2 на services.gradle.org.
- `./gradlew --version`: Gradle 9.8.2, Launcher JVM 26.0.2.1, Daemon JVM — текущая Java 26. То есть Gradle 9.8.2 запускается на локальной Java 26, а компиляция и тесты идут на toolchain 21.

### Проверки

Все запуски выполнены из корня репозитория через `./gradlew`, `JAVA_HOME` не задан. Для проверок временно добавлялись класс `Phase1DiagnosticsTest extends TestBase` (печать системных свойств, `Configuration`, `WebDriverRunner.hasWebDriverStarted()`, URL, session id, размера окна, `navigator.userAgent` и дочерних процессов JVM через `ProcessHandle.current().descendants()`) и временный `@AfterAll` в `SearchTests`. После проверок всё удалено; `grep` по `src` подтвердил отсутствие следов диагностики.

- Toolchain: `./gradlew javaToolchains` нашёл JDK 17, 21, 24 и 26 локально; auto-provisioning не настраивался. В JVM тестов `java.version=21.0.12.1`, у `TestBase.class` major version 65 (Java 21).
- Defaults (`./gradlew test --tests Phase1DiagnosticsTest -Dselenide.timeout=1 -Dfoo=bar --rerun-tasks`): 3/3. В JVM тестов нет ни одного свойства `selenide.*` и нет `foo` — лишние свойства не передаются. `Configuration`: `baseUrl=https://github.com`, `browser=chrome`, `browserSize=1366x768`, `headless=false`, `timeout=4000` (default Selenide, `-Dselenide.timeout=1` не дошёл). `open("/search")` → `https://github.com/search`, окно 1366x768, UA без `Headless`.
- Переопределение без браузера (`--tests Phase1DiagnosticsTest.configOnly -Dselenide.baseUrl=https://example.com -Dselenide.browser=dummybrowser -Dselenide.browserSize=1920x1080 -Dselenide.headless=true`): 1/1. В JVM тестов ровно четыре переданных свойства, `Configuration` содержит все четыре значения, включая `browser=dummybrowser`.
- Настройка не запускает браузер: после `@BeforeEach` `hasWebDriverStarted=false`, дочерних процессов JVM 0 — и с defaults, и с `browser=dummybrowser` (тест прошёл, значит драйвер не создавался).
- Использование `baseUrl` внутри JVM тестов (`--tests Phase1DiagnosticsTest -Dselenide.baseUrl=https://example.com -Dselenide.browser=chrome -Dselenide.browserSize=1920x1080 -Dselenide.headless=true`): 3/3, `open("/search")` → `https://example.com/search`, окно 1920x1080, UA `HeadlessChrome/155.0.0.0`.
- Изоляция сессий (в обоих полных прогонах диагностики): перед каждым тестом `hasWebDriverStarted=false` и 0 дочерних процессов; два теста получили разные session id; во время теста дочерние процессы — новые `chromedriver` и `Google Chrome` с другими PID.
- Cleanup после успеха: временный `@AfterAll` в `SearchTests` при исходном assertion — 1/1, `hasWebDriverStarted=false`, 0 дочерних процессов до выхода JVM; после прогона `pgrep chromedriver` пуст.
- Cleanup после контролируемого падения: assertion временно заменён на `text("QA.GURU-PHASE1-CONTROLLED-FAIL")`. Результат: `SearchTests > successfulSearchTest() FAILED`, `com.codeborne.selenide.ex.ElementShould at SearchTests.java:19`, BUILD FAILED, exit code 1, `failures="1"`. В `@AfterAll` `hasWebDriverStarted=false`, 0 дочерних процессов; `pgrep chromedriver` пуст. Исходный assertion восстановлен (`git diff` `SearchTests` содержит только `extends TestBase` и `/search`).

### Финальные запуски (после удаления диагностики)

- Первая команда (defaults, headed): BUILD SUCCESSFUL in 45s, exit code 0, `TEST-SearchTests.xml`: `tests="1" skipped="0" failures="0" errors="0"`, `time="44.021"`.
- Вторая команда (`https://www.github.com`, chrome, 1920x1080, headless): BUILD SUCCESSFUL in 7s, exit code 0, `tests="1" skipped="0" failures="0" errors="0"`, `time="6.988"`.
- После обоих запусков `pgrep chromedriver` пуст.

### Ограничения и непроверенное

- Headed-окно на macOS ограничено экраном: с `-Dselenide.browserSize=1920x1080` без headless фактический размер 1728x999 (дисплей Retina 3456x2234). 1366x768 в headed и 1920x1080 в headless применяются точно.
- stderr тестов: предупреждение SLF4J об отсутствии провайдера (NOP logger) и предупреждение Selenium о CDP: Chrome 155.0.8059.85, ближайшая поддерживаемая Selenium 4.49.0 версия CDP — 153. На прохождение тестов не влияли.
- Проверка `browser=dummybrowser` подтверждает только чтение значения в `Configuration`; запуск других браузеров не проверялся. Headless — по UA `HeadlessChrome`.
- Не проверялись: `gradlew.bat` на Windows, запуск из IDE, чистый checkout, configuration cache, параллельный запуск тестов.
- Успешные запуски не гарантируют доступность GitHub и неизменность результатов по `qa.guru` (см. ограничения фазы 0). HTTP 429 в этой фазе не наблюдался.
- `gradlew.bat`, который сгенерировал Gradle, имеет CRLF-окончания во всех 112 строках; после удаления CR в конце строк пробелов нет. Без `.gitattributes` `git diff --check` выдавал на него 112 предупреждений `trailing whitespace`. С `.gitattributes` после `git add --renormalize` в индексе хранится LF, в рабочем дереве остаётся CRLF, `git diff --check` проходит без предупреждений.
- Chromedriver: новых каталогов в `~/.cache/selenium/chromedriver/mac-arm64` не появилось (последний — `155.0.8059.84` от фазы 0), хотя у Chrome версия 155.0.8059.85.
- `bin/` теперь игнорируется git и в коммит не входит.

## Фаза 2 — Tests

Статус: реализована 2026-10-20 в ветке `phase-2`, созданной от `master` после fast-forward до `34349bf` (merge PR #2, фаза 1). Один коммит фазы. По решению пользователя 2026-10-20 ветка запушена в `origin/phase-2` и для неё открыт PR #3 в `master`; PR #3 влит в `master`. Отдельным коммитом в плане обновлены сведения о push и PR. Сценарии подтверждены пользователем 2026-10-20 по итогам этапа A, включая проверку счётчика `0` во втором сценарии. Фаза 3 ожидает подтверждения пользователя.

Объём фазы:

- Этап A: предложить два независимых сценария поиска через `/search`, проверить их в реальном UI, зафиксировать цели, предусловия, данные, assertions, локаторы, риски и зависимость от неконтролируемых данных; получить подтверждение.
- Этап B: добавить два независимых теста в SearchTests, сохранив исходный сценарий (`qa.guru` → `QA.GURU`); TestBase, относительный `/search`, ожидания Selenide; проверить каждый тест отдельно, весь набор с defaults и в headless; установить причину каждого падения; удалить диагностику; коммит.

### Подтверждённые сценарии

`repoQualifierSearchTest` — поиск внутри репозитория:

- Цель: квалификатор `repo:` ограничивает выдачу одним репозиторием.
- Предусловия: пользователь не авторизован; репозиторий `selenide/selenide` существует и в нём есть хотя бы один issue.
- Данные: запрос `repo:selenide/selenide`.
- Assertions: в выдаче есть хотя бы один результат (`sizeGreaterThan(0)`), и у каждого результата ссылка на репозиторий имеет ровно текст `selenide/selenide` (`filterBy(not(exactText(...)))` → `size(0)`).
- Локатор: `[data-testid='results-list'] a[data-hovercard-type='repository']`.
- Обоснование: с `repo:` GitHub сам выбирает `type=issues`, а пункта «Repositories» в боковой навигации нет (проверено в UI), поэтому найти сам репозиторий через `repo:` нельзя. Тест проверяет инвариант области поиска, который не зависит от того, какие issues найдены, в каком порядке и сколько их.
- Зависимость от данных: набор, порядок и количество issues (около 1.4k на 2026-10-20) не контролируются; тест требует только наличия хотя бы одного результата.

`searchWithoutResultsTest` — поиск без результатов:

- Цель: на запрос без совпадений показывается сообщение о пустой выдаче, счётчик репозиториев равен 0, списка результатов нет.
- Предусловия: пользователь не авторизован.
- Данные: запрос `qaguruphase2nomatchzx7q9w`.
- Assertions по порядку: виден заголовок `Your search did not match any repositories` (это же ожидание отрисовки выдачи); счётчик в пункте «Repositories» боковой навигации имеет ровно текст `0`; после этого `[data-testid='results-list']` не существует. Проверка отсутствия идёт последней, чтобы не пройти до загрузки выдачи.
- Локаторы: `byRole("heading", "Your search did not match any repositories")`; `[data-testid='nav-item-repositories'] [data-testid='resolved-count-label']`; `[data-testid='results-list']`.
- Зависимость от данных: тест упадёт, если появится репозиторий с таким именем; надпись проверяется по английскому тексту.

Тесты независимы: в каждом свой `open("/search")` и своя сессия браузера из TestBase, общего состояния нет. `successfulSearchTest` не менялся.

### Изменения

- `src/test/java/SearchTests.java`: добавлены два метода и статические импорты `CollectionCondition.size/sizeGreaterThan`, `Condition.exactText/exist/not/visible`, `Selectors.byRole`, `Selenide.$$`. TestBase, `build.gradle`, версии зависимостей и свойства не менялись.

### Проверки

Все запуски выполнены из корня репозитория через `./gradlew`, `JAVA_HOME` не задан. Результаты — по `build/test-results/test/TEST-*.xml`.

Этап A: временный класс `Phase2DiagnosticsTest extends TestBase` в `src/test/java` (запись URL, `data-testid`, разметки выдачи и заголовка в файл вне репозитория), 3 headless-прогона по 2 поиска:

1. Прошёл: `repo:selenide/selenide` открывает `…?q=repo%3Aselenide%2Fselenide&type=issues`.
2. Упал на клике `a[href*='type=repositories']` (`ElementNotFound`). Причина — неверное предположение диагностики: с `repo:` пункта «Repositories» в навигации нет. Сценарий переформулирован на инвариант области поиска.
3. Прошёл: 10 результатов, у каждого ровно одна ссылка `data-hovercard-type='repository'` на `/selenide/selenide`, других hovercard-ссылок в выдаче нет; черновой assertion сценария 1 прошёл. Пустая выдача: `…&type=repositories`, `<h3>Your search did not match any repositories</h3>`, `results-list` отсутствует.

Этап B:

- `./gradlew test --tests "SearchTests.repoQualifierSearchTest" --rerun-tasks --console=plain`: 1/1, `time="142.858"` (причина длительности не установлена, см. ниже). Повтор на финальной версии файла: BUILD SUCCESSFUL in 41s, `tests="1" skipped="0" failures="0" errors="0"`.
- `./gradlew test --tests "SearchTests.searchWithoutResultsTest" --rerun-tasks --console=plain`: два падения подряд, затем исправление локаторов и 1/1:
  - Исходный локатор `byText(...)`: `ElementNotFound`, воспроизводится. HTTP 429 нет: на скриншоте нормальная страница пустой выдачи, а `429` в сохранённом page source встречается только в хешах ассетов и SVG-путях. Временная диагностика (headed): текст `did not match` в `body` появляется через 561 мс после Enter, но `byText` не находит элемент и за 60 с. Причина: в живом DOM `<h3>` состоит из двух текстовых узлов — `"Your search did not match any "` и `"repositories"`, а XPath Selenide `byText` сравнивает каждый `text()`-узел отдельно (`getPageSource` склеивает узлы, поэтому в page source текст цельный). На этапе A надпись проверялась только через `$("body").shouldHave(text(...))`. Локатор заменён на `byRole("heading", ...)` (Selenide 7.17+, имя по `textContent`): точное совпадение полного текста с учётом регистра сохранено.
  - Счётчик `[data-testid='type-switcher'] [data-testid='resolved-count-label']`: `ElementShould`, `displayed:false`, `text=""`. Причина: `type-switcher` — переключатель узкой вёрстки, при 1366x768 скрыт; на этапе A счётчик читался через JS `textContent`, который видит скрытые элементы. Локатор заменён на видимый счётчик боковой навигации `nav-item-repositories`.
  - Финально: BUILD SUCCESSFUL in 12s, `tests="1" skipped="0" failures="0" errors="0"`.
- `./gradlew test --rerun-tasks --console=plain` (defaults, headed, 1366x768): BUILD SUCCESSFUL in 36s, `tests="3" skipped="0" failures="0" errors="0"`; `successfulSearchTest` 12.4 с, `repoQualifierSearchTest` 11.8 с, `searchWithoutResultsTest` 11.9 с.
- `./gradlew test -Dselenide.headless=true --rerun-tasks --console=plain`: BUILD SUCCESSFUL in 29s, `tests="3" skipped="0" failures="0" errors="0"`; 11.2 с, 5.5 с, 11.9 с.
- После последнего прогона `pgrep chromedriver` пуст.
- Временная диагностика удалена; `grep` по `src` на `diag|Phase2|probe|System.out|executeJavaScript|byText|type-switcher` находит только тестовые данные `qaguruphase2nomatchzx7q9w` в `SearchTests`.

### Ограничения и непроверенное

- `repoQualifierSearchTest` полагается на то, что GitHub для `repo:` выбирает выдачу issues; тип выдачи тест не проверяет. Если GitHub выберет поиск по коду (для анонимного пользователя требует входа), тест упадёт.
- Локаторы опираются на внутреннюю разметку GitHub: `data-hovercard-type`, `data-testid` (`results-list`, `nav-item-repositories`, `resolved-count-label`), роль и английский текст заголовка. Изменение разметки или текста сломает тесты.
- Счётчик проверяется в боковой навигации, видимой на широкой вёрстке. При узком `selenide.browserSize` навигация может скрываться (вместо неё `type-switcher`) — не проверялось. В фазе 2 запуски шли только с 1366x768 (headed и headless); `1920x1080` из базовых команд не запускался.
- Наблюдались разные варианты страницы пустой выдачи: в headless на этапе A подсказка «Try one of the tips below to find more repositories», в headed на этапе B — «You could try one of the tips below.» и счётчики `0` у всех типов. Причина (A/B-тест, вёрстка, время) не установлена; заголовок и счётчик Repositories совпадали в обоих вариантах.
- Аномальная длительность: первый отдельный запуск `repoQualifierSearchTest` — 142.9 с, в одном диагностическом headed-прогоне `open("/search")` — 22.6 с. Новых каталогов chromedriver в `~/.cache/selenium` не появилось; причина не установлена, на результат не повлияла.
- HTTP 429 в фазе не наблюдался. Однократные успешные запуски не гарантируют стабильность внешнего GitHub и неизменность данных.
- Не проверялись: запуск из IDE, параллельный запуск тестов, другие браузеры, авторизованный пользователь.

## Фаза 3 — Page Object

Статус: реализована 2026-10-20 в ветке `phase-3`, созданной от `master` после fast-forward до `0cabb86` (merge PR #3, фаза 2). Коммит фазы и отдельный коммит с расследованием длительности тестов в плане. Ветка запушена в `origin/phase-3`, PR #4 влит в `master`. Структура подтверждена пользователем 2026-10-20 по итогам этапа A: BasePage — вариант с `openPath`, проверки пустой выдачи — три отдельных вызова в тесте. Фаза 4 ожидает подтверждения пользователя.

Объём фазы:

- Подтвердить применимость согласованного стиля к фактическим сценариям.
- Перенести тесты и TestBase в пакет `tests`, страницы разместить в пакете `pages`.
- Перенести взаимодействия и локаторы в страницы, проверки — в методы страниц.
- Добавить BasePage и компоненты сразу, по решению фазы 0.
- Сохранить локаторы, тестовые данные, assertions и порядок проверок; проверить каждый тест отдельно, весь набор с defaults и в headless; закоммитить изменения.

### Подтверждённая структура

Разбор тестов: во всех трёх повторяются `open("/search")` и ввод запроса с Enter в `[aria-label='Search GitHub']`; на странице выдачи логически самостоятельны поле поиска, область результатов (`results-list` и пустое состояние с заголовком) и боковая навигация по типам выдачи со счётчиком.

- `pages.BasePage` — `abstract`, единственный метод `protected void openPath(String path)` → `Selenide.open(path)`. Общего поведения у страниц пока нет (страница одна); метод — единое место для правила «пути относительны к `baseUrl`».
- `pages.SearchPage extends BasePage` — fluent-фасад: компоненты в `private final`-полях, все публичные методы возвращают `SearchPage`: `openPage()` (`openPath("/search")`), `search(query)`, `shouldHaveResultsWithText(text)`, `shouldHaveOnlyResultsFromRepository(repository)`, `shouldShowNoMatchingRepositoriesMessage()`, `shouldHaveRepositoriesCount(count)`, `shouldNotHaveResultsList()`. Методы проверок делегируют компонентам. Одна страница, а не две: URL тот же (`/search?q=…`), поле поиска остаётся на странице выдачи. Метод назван `openPage()`, потому что метод `open()` перекрыл бы статический импорт `Selenide.open` в классе.
- `pages.components.SearchInputComponent` — `search(query)`.
- `pages.components.SearchResultsComponent` — поле `private final SelenideElement resultsList` (`[data-testid='results-list']` используется дважды); `shouldHaveText`, `shouldHaveOnlyRepository` (цепочка `$$` без изменений), `shouldShowNoMatchingRepositoriesMessage` (`byRole`), `shouldNotExist`.
- `pages.components.SearchTypeNavigationComponent` — `shouldHaveRepositoriesCount(count)`.
- Остальные элементы используются по одному разу и ищутся `$()`/`$$()` внутри методов. Компоненты без корневого элемента и без общего родителя: поиск относительно корня (`resultsList.$$(…)`) изменил бы локатор, поэтому селекторы остались полными строками.
- Тестовые данные (запросы и ожидаемые `QA.GURU`, `selenide/selenide`, `0`) передаются из тестов; текст заголовка остаётся в компоненте как часть локатора `byRole`.
- В `searchWithoutResultsTest` проверки — три отдельных вызова в цепочке теста в прежнем порядке; `shouldNotHaveResultsList()` последний (причина — в разделе фазы 2).

### Изменения

- `src/test/java/TestBase.java` → `src/test/java/tests/TestBase.java`: добавлена только строка `package tests;`, логика не менялась.
- `src/test/java/SearchTests.java` → `src/test/java/tests/SearchTests.java`: `package tests;`, поле `SearchPage`, тесты — fluent-цепочки страницы.
- Новые: `src/test/java/pages/BasePage.java`, `src/test/java/pages/SearchPage.java`, `src/test/java/pages/components/SearchInputComponent.java`, `SearchResultsComponent.java`, `SearchTypeNavigationComponent.java`.
- `build.gradle`, версии зависимостей и свойства `selenide.*` не менялись.

### Проверки

Все запуски выполнены из корня репозитория через `./gradlew`, `JAVA_HOME` не задан, без временной диагностики в `src`. Результаты — по `build/test-results/test/TEST-tests.SearchTests.xml`.

- `./gradlew test --tests "tests.SearchTests.successfulSearchTest" --rerun-tasks --console=plain`: exit code 0, `tests="1" skipped="0" failures="0" errors="0"`, 15.7 с (первый запуск без захвата exit code — тоже 1/1, 13.8 с).
- `./gradlew test --tests "tests.SearchTests.repoQualifierSearchTest" --rerun-tasks --console=plain`: exit code 0, 1/1, 41.0 с.
- `./gradlew test --tests "tests.SearchTests.searchWithoutResultsTest" --rerun-tasks --console=plain`: exit code 0, 1/1, 41.1 с.
- `./gradlew test --rerun-tasks --console=plain` (defaults, headed, 1366x768): BUILD SUCCESSFUL in 44s, `tests="3" skipped="0" failures="0" errors="0"`; 11.1 с, 16.3 с, 16.9 с.
- `./gradlew test -Dselenide.headless=true --rerun-tasks --console=plain`: BUILD SUCCESSFUL in 1m 45s, `tests="3" skipped="0" failures="0" errors="0"`; 16.7 с, 42.2 с, 46.3 с.
- После каждого запуска `pgrep chromedriver` пуст. Падений не было.
- Сохранение поведения относительно `master`: множества различных строковых литералов (локаторы, тестовые данные, `heading`, `/search`) в `master:src/test/java/SearchTests.java` и в новых файлах совпадают (`grep -ohE '"[^"]*"' | sort | uniq` + `diff`); так же совпадают множества вызовов `open/setValue/pressEnter/shouldHave/shouldBe/shouldNot/filterBy/byRole/text/exactText/sizeGreaterThan/size/not/exist/visible`. Кратность меньше только из-за свёртки повторов (`/search` и поле поиска — 3 → 1, `results-list` — 2 → 1 поле). Строки assertions в компонентах совпадают с `master` дословно, кроме подстановки параметров; порядок вызовов в тестах совпадает с порядком проверок в `master`. `diff` TestBase с `master` — только `package tests;` и пустая строка.
- `grep` по `src` на `diag|Phase3|probe|System.out|executeJavaScript|sleep|@Disabled|byText|type-switcher` ничего не находит.

### Ограничения и непроверенное

- Длительность тестов в этой фазе заметно выше, чем в фазе 2 (например, `repoQualifierSearchTest` в headless 42.2 с против 5.5 с, отдельные запуски около 41 с). Причина установлена (см. «Расследование длительности»): медленная загрузка статических ресурсов GitHub по медленному сетевому каналу, перенос на Page Object на неё не влияет. HTTP 429 не наблюдался.
- Перенос выполнен `git mv`. `TestBase` git показывает как rename (97%); `SearchTests` изменён почти целиком (41 → 31 строка, общих строк мало), поэтому git показывает его как удаление `src/test/java/SearchTests.java` и добавление `src/test/java/tests/SearchTests.java`.
- Ограничения фазы 2 (внутренняя разметка GitHub, английский текст заголовка, неконтролируемые данные, боковая навигация при узком окне) действуют без изменений.
- Не проверялись: запуск с `-Dselenide.browserSize=1920x1080` из базовых команд, запуск из IDE, параллельный запуск тестов, другие браузеры.

### Расследование длительности

Выполнено 2026-10-20 по запросу пользователя после коммита фазы. Код тестов и страниц не менялся.

- A/B: `master` (`0cabb86`, временный detached worktree вне репозитория) и `phase-3` поочерёдно, `./gradlew test -Dselenide.headless=true --rerun-tasks --console=plain`. `master` №1: 45.1 / 44.9 / 45.0 с, BUILD FAILED; `phase-3` №1: 14.8 / 12.4 / 18.1 с, 3/3; `master` №2: 12.4 / 12.8 / 10.9 с, 3/3; `phase-3` №2: 8.6 / 8.7 / 9.7 с, 3/3. Длительность меняется от запуска к запуску одинаково для обеих версий, значит причина не в переносе.
- Падение `master` №1: `repoQualifierSearchTest` — `ListSizeMismatch` на `sizeGreaterThan(0)` (`SearchTests.java:28`). На скриншоте Selenide — обычная выдача issues `selenide/selenide` (1.4k results), признаков HTTP 429 нет; в page source, снятом после истечения ожидания, 10 ссылок `data-hovercard-type="repository"`. То есть результаты отрисовались позже 4-секундного таймаута Selenide — проявление той же медленной загрузки, не ошибка локатора.
- Разбивка по шагам: временный `@RepeatedTest(4)`-класс в `src/test/java/tests` повторял сценарий `repoQualifierSearchTest` с замерами и Navigation/Resource Timing браузера (4 запуска headed, 4 headless). Старт драйвера 0.4–0.6 с; `open("/search")` 8.6–17.6 с; `setValue` + `pressEnter` < 0.1 с; появление результатов после Enter 0.2–0.8 с. Внутри `open`: HTML получен за 0.6–2.0 с (`responseEnd`), `domContentLoaded` — 8.6–17.6 с; 67–100 JS-ресурсов `github.githubassets.com` грузятся по 3–16 с каждый. Поиск после Enter — SPA-переход без новой навигации.
- Сеть вне браузера (curl): 97 JS-ресурсов страницы `/search` (около 4 МБ без сжатия) в 6 потоков — 16.8 с, примерно 240 КБ/с, один поток 30–80 КБ/с. Тот же файл `three.min.js` 0.128 (603 КБ) с cdnjs.cloudflare.com и cdn.jsdelivr.net — около 6.8 с (около 88 КБ/с). Канал медленный в целом, а не только до GitHub.
- Вывод: каждый тест получает новую сессию браузера с пустым кэшем, а `open()` ждёт загрузки страницы, поэтому время теста определяется скоростью загрузки примерно сотни JS-ресурсов GitHub и колеблется вместе со скоростью сети. При особенно медленной загрузке результаты могут появиться позже таймаута Selenide (4 с) — тогда тест падает, как `master` №1. Причина медленного канала (провайдер, Wi-Fi, VPN) не устанавливалась; в фазе 2 тот же код выполнялся за 5.5–12.4 с.
- Временный диагностический класс и worktree удалены; `grep` по `src` на `diag|Phase3|probe|System.out|executeJavaScript|sleep|@Disabled|RepeatedTest|performance|scratchpad` ничего не находит.
- Таймауты, стратегию загрузки страницы и кэш браузера не меняли: это изменило бы поведение тестов и логику TestBase и требует отдельного согласования.

## Фаза 4 — Allure Report 3

Статус: реализована 2026-10-20 в ветке `phase-4`, созданной от `master` после fast-forward до `382986a` (merge PR #4, фаза 3). Один коммит. Ветка запушена в `origin/phase-4`, PR #5 влит в `master`. Решения этапа A подтверждены пользователем 2026-10-20 (все предложенные варианты). Фаза 5 ожидает подтверждения пользователя.

Объём фазы:

- Этап A: сравнить генераторы, подобрать JVM-адаптеры и версии, предложить шаги, вложения и расположение результатов и отчёта, получить подтверждение.
- Этап B: установить генератор, подключить адаптеры и AspectJ, добавить шаги и вложения, проверить успешные прогоны, контролируемое падение и отчёт, удалить диагностику, закоммитить.

### Решения этапа A

Генератор отчёта:

| | Allure Report 3 (npm `allure`) | Allure Report 2 (CLI) |
|---|---|---|
| Версия на 2026-10-20 | 3.21.0 (2026-10-16) | 2.46.1 (2026-09-03) |
| Установка на macOS | `npm install allure` в проект или `-g`; нужен Node.js, поле `engines` в пакете не задано | `brew install allure` или zip/tgz из GitHub Releases + PATH; Java 8+ |
| CI | `npm ci` + `npx allure` | скачать zip нужной версии |
| Что появляется в проекте | `package.json`, `package-lock.json`, `node_modules/` (игнорируется) | ничего |
| Закрепление версии | точная версия + lock-файл, включая транзитивные пакеты | brew — только последняя версия; zip — по имени каталога |
| Формат Allure Java | «Results produced by Allure Java 3 can be viewed in both Allure Report 3 and Allure Report 2» (migration guide) | то же |

- Выбран Allure Report 3.21.0 как devDependency: одна закреплённая версия локально и в CI (`npm ci`), Node.js уже установлен. Отклонены: `npx allure@3.21.0` без `package.json` (транзитивные зависимости заданы диапазонами) и Gradle-плагин `io.qameta.allure` 4.3.0 (сам скачивает Node/CLI и объединяет адаптеры с генератором).
- Allure Java 3.0.0 (2026-09-11): требует Java 17+; `allure-junit5` больше не публикуется, замена — `allure-jupiter`. По документации `allure-jupiter` «works with both JUnit 6 and JUnit 5»; README модуля: сборка проверяется на JUnit 6.1.0. `allure-selenide`: «targets Selenide 7.x», в README — проверено на 7.17.0, модуль собран с 7.18.1; 7.18.2 явно не заявлена. Allure Java 3 больше не очищает каталог результатов — это задача сборки.
- AspectJ weaver 1.9.25.1 (2025-12-17) — его использует сборка allure-java 3.0.0; в документации Allure указана 1.9.25. Поддержка Java 25 появилась в 1.9.25, Java 21 покрыта.
- `@Step` — только на публичных методах `SearchPage` (фасад); на компонентах нет, чтобы не дублировать шаги. Названия шагов — на английском. Вариант `Allure.step(...)` без агента отклонён: он меняет тела методов.
- `AllureSelenide`: `screenshots(true)`, `savePageSource(true)`, `includeSelenideSteps(true)`; браузерные логи не включаются. Регистрация — в `@BeforeAll static` в `TestBase`, как в документации Selenide-интеграции; `setUp` и `tearDown` не менялись.
- Результаты — `build/allure-results` через `src/test/resources/allure.properties` (рекомендация документации JUnit Jupiter); передача свойств из Gradle не затронута. Очистка — `doFirst { delete … }` в задаче `test`. Отклонён вариант `./gradlew clean test`: он стирает весь `build/` и держится на дисциплине. Отчёт — `build/allure-report`, `generate --clean`. Файл `allurerc.*` не нужен: каталог вывода и очистка задаются опциями CLI.

Источники:

- https://github.com/allure-framework/allure-java/releases/tag/3.0.0; README `allure-jupiter`, `allure-junit-platform`, `allure-selenide`, `allure-java-commons` и `build.gradle.kts` в теге 3.0.0
- https://allurereport.org/docs/guides/allure-java-3-migration/
- https://allurereport.org/docs/junit5/
- https://allurereport.org/docs/selenide/
- https://allurereport.org/docs/v3/install/, https://allurereport.org/docs/v3/configure/, https://github.com/allure-framework/allure3/releases/tag/v3.21.0 (`packages/cli/src/commands/generate.ts`, `open.ts`)
- https://allurereport.org/docs/v2/install-for-macos/, https://github.com/allure-framework/allure2/releases/tag/2.46.1
- https://github.com/eclipse-aspectj/aspectj/releases/tag/V1_9_25_1, `docs/release/README-1.9.25.adoc`
- https://github.com/allure-framework/allure-gradle/releases/tag/4.3.0
- Maven Central: `maven-metadata.xml` и POM артефактов 3.0.0; `npm view allure`

### Изменения

- `build.gradle`: `testImplementation platform("io.qameta.allure:allure-bom:3.0.0")`, `io.qameta.allure:allure-jupiter`, `io.qameta.allure:allure-selenide`; конфигурация `agent` (`canBeResolved = true`, `canBeConsumed = false`) с `org.aspectj:aspectjweaver:1.9.25.1`; в `test` — `jvmArgs "-javaagent:${configurations.agent.singleFile}"` и `doFirst { delete layout.buildDirectory.dir("allure-results") }`. Версии Java, JUnit, Selenide и передача свойств `selenide.*` не менялись.
- `src/test/resources/allure.properties` (новый): `allure.results.directory=build/allure-results`.
- `src/test/java/tests/TestBase.java`: метод `@BeforeAll static void addAllureListener()` — `SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(true).includeSelenideSteps(true))`.
- `src/test/java/pages/SearchPage.java`: `@Step` на 7 публичных методах — `Open search page`, `Search for "{query}"`, `Check that results contain text "{text}"`, `Check that all results are from repository "{repository}"`, `Check that no matching repositories message is shown`, `Check that repositories count is "{count}"`, `Check that results list is absent`. Сигнатуры и тела методов не менялись.
- `package.json`, `package-lock.json` (новые): `npm install --save-dev --save-exact allure@3.21.0` → `devDependencies.allure = "3.21.0"`, lockfileVersion 3, 158 пакетов.
- `.gitignore`: `node_modules/`.

### Проверки

Все запуски выполнены из корня репозитория, `JAVA_HOME` не задан, npm и Node — из `/opt/homebrew/bin` (Node 26.9.0, npm 11.19.1).

- `npx allure --version` → `3.21.0`. `npx allure generate --help`: есть `--output,-o`, `--clean` («Remove the output directory before generating the report»), `--open`, `--port`, `--host`. `npx allure open --help`: путь к отчёту или к результатам, `--port`, `--host`. По умолчанию сервер слушает все сетевые интерфейсы, поэтому локально используется `--host 127.0.0.1`.
- `./gradlew compileTestJava`: успешно. `./gradlew dependencies` и `dependencyInsight --configuration testRuntimeClasspath`: `allure-jupiter`, `allure-selenide`, `allure-java-commons`, `allure-junit-platform` — 3.0.0; транзитивные `junit-jupiter-api` и `junit-platform-launcher` 6.1.0 разрешаются в 6.1.3; Selenide 7.18.2.
- `./gradlew test --rerun-tasks --console=plain` (defaults, headed): BUILD SUCCESSFUL in 16s, exit code 0, `tests="3" skipped="0" failures="0" errors="0"` (5.5 / 4.4 / 5.0 с). В `build/allure-results` 3 файла `*-result.json` (по одному на тест) и 4 `*-container.json`. В каждом результате есть дерево шагов: пользовательские шаги `@Step` с параметрами (`query`, `text`, `repository`, `count`) и вложенные шаги Selenide (`$("open") https://github.com/search` → `$("webdriver") create()`, `set value`, `press enter`, `should have(...)`). Последний шаг на уровне теста — `$("webdriver") close()` из `@AfterEach`.
- `npx allure generate build/allure-results --output build/allure-report --clean`: exit code 0, Awesome-отчёт в `build/allure-report`; каталог `allure-report/` в корне репозитория не создан.
- Контролируемое падение: в `successfulSearchTest` ожидаемый текст временно заменён на `QA.GURU-PHASE4-CONTROLLED-FAIL`. `./gradlew test --rerun-tasks --console=plain`: `SearchTests > successfulSearchTest() FAILED`, `com.codeborne.selenide.ex.ElementShould at SearchTests.java:13`, BUILD FAILED in 32s, exit code 1, `tests="3" failures="1"`.
  - В результате Allure тест `failed`, сообщение `Element should have text "QA.GURU-PHASE4-CONTROLLED-FAIL" {[data-testid='results-list']}`, trace на 179 строк (`SearchPage.shouldHaveResultsWithText(SearchPage.java:27)`, `SearchTests.successfulSearchTest(SearchTests.java:13)`).
  - Шаг `Check that results contain text "QA.GURU-PHASE4-CONTROLLED-FAIL"` — `failed`; вложенный шаг Selenide `should have(text ...)` — `failed`, с вложениями `Screenshot` (image/png, 358 КБ) и `Page source` (text/html, 249 КБ).
  - Причина падения — assertion: на скриншоте обычная выдача (`10.5k results`, `qa-guru/niffler`…); `429` в page source встречается только в хеше JS-файла и SVG-путях, «Too many requests» нет.
  - После прогона `pgrep chromedriver` пуст, браузерных процессов нет.
- Отчёт с падением сгенерирован в scratchpad (вне репозитория) и открыт командой `npx allure open <dir> --host 127.0.0.1 --port 47801` (HTTP 200). Снимки headless Chrome: на главной странице Total 3, Failed 1, Passed 2; на странице `successfulSearchTest()` — статус Failed, блок Error с сообщением assertion, вкладка Attachments 2. Отчёт передан пользователю для визуальной приёмки; её результат в плане не зафиксирован.
- Assertion восстановлен: `git diff` `SearchTests.java` пуст.
- `./gradlew test -Dselenide.headless=true --rerun-tasks --console=plain`: BUILD SUCCESSFUL in 17s, exit code 0, 3/3 (6.4 / 4.9 / 5.0 с). В `build/allure-results` ровно 3 результата: результаты прошлого прогона удалены `doFirst`.
- Финальный запуск `./gradlew test --rerun-tasks --console=plain`: BUILD SUCCESSFUL in 20s, exit code 0, 3/3 (6.4 / 5.3 / 8.0 с), 3 результата, 0 вложений. `npx allure generate … --clean` — exit code 0, в отчёте 3 теста. Отчёт открыт командой `npx allure open build/allure-report --host 127.0.0.1 --port 47802` (HTTP 200). `pgrep chromedriver` пуст.
- Других падений, кроме контролируемого, не было; медленная сеть из фазы 3 в этих прогонах не проявилась.
- Временная диагностика в `src` не создавалась, кроме временной замены assertion. `grep` по `src` на `PHASE4|CONTROLLED|diag|probe|System.out|sleep|@Disabled|RepeatedTest|executeJavaScript` ничего не находит. `git check-ignore`: `node_modules`, `build/allure-results` и `build/allure-report` игнорируются.

### Ограничения и непроверенное

- Совместимость версий: `allure-jupiter` 3.0.0 проверен разработчиками на JUnit 6.1.0, `allure-selenide` — на Selenide 7.17.0 и 7.18.1. С JUnit 6.1.3 и Selenide 7.18.2 совместимость подтверждена только прогонами этой фазы.
- Allure Report 3.21.0 вышел за 4 дня до фазы. Минимальная версия Node.js пакетом не заявлена; проверена только Node 26.9.0.
- Вложения создаёт `AllureSelenide` только при падении события Selenide. Падение вне Selenide (например, исключение в коде теста) не даст скриншота и page source.
- `doFirst` очищает результаты при каждом запуске `test`, поэтому после запуска с `--tests` в отчёте будут только выбранные тесты. История отчёта (`historyPath`) не настраивалась.
- `allure.results.directory` задан относительным путём: при запуске из IDE с другим рабочим каталогом результаты окажутся в другом месте. Не проверялось.
- `npx allure open` блокирует терминал до Ctrl+C и по умолчанию слушает все интерфейсы.
- `jvmArgs` с `configurations.agent.singleFile` разрешает конфигурацию `agent` на этапе конфигурации Gradle; configuration cache не проверялся.
- В stderr тестов осталось только предупреждение Selenium о CDP 155/153. Предупреждение SLF4J о NOP logger из прошлых фаз в stderr больше не появлялось; причина не исследовалась.
- Для Jenkins (фаза 7) на агенте понадобится Node.js; Allure Jenkins plugin использует Report 2. Это решается в фазе 7.
- Не проверялись: запуск из IDE, чистый checkout с `npm ci`, `-Dselenide.browserSize=1920x1080`, Classic-отчёт Allure 3, Windows.

## Фаза 5 — GitHub Actions

Статус: реализована 2026-10-20 в ветке `phase-5`, созданной от `master` после fast-forward до `04b8880` (merge PR #5, фаза 4). Коммит с workflow и коммит с обновлением плана. По решению пользователя ветка запушена в `origin/phase-5`, в `master` не влита. Решения этапа A подтверждены пользователем 2026-10-20 (все предложенные варианты). Временные ветки проверок ожидают подтверждения удаления. Фаза 6 ожидает подтверждения пользователя.

Объём фазы:

- Этап A: согласовать события запуска, публикацию отчёта, inputs, браузер и драйвер, Node.js и Allure, версии actions, permissions и secrets, внешние действия и способ локальной проверки.
- Этап B: добавить workflow, проверить его локально (actionlint) и в реальном GitHub Actions: успешный запуск, контролируемое падение assertion, ошибку компиляции, отсутствие результатов; удалить временные изменения, обновить план, закоммитить.

### Решения этапа A

Репозиторий для CI — `origin` (https://github.com/Dumowka/getting-started-java): публичный, default branch `master`. На 2026-10-20 Actions включены (`allowed_actions: all`), `default_workflow_permissions: read`, Pages не включён, environments и workflows не было.

- События: `workflow_dispatch` и `push` во все ветки (`branches: ['**']`, теги не запускают) с `paths-ignore` `docs/**`, `**/*.md`. `pull_request` не добавлен: для одного коммита он дал бы второй прогон к push и лишние запросы к github.com (риск HTTP 429). `schedule` не добавлен: работает только на default branch, в публичном репозитории отключается после 60 дней без активности, регулярные анонимные поиски повышают риск 429.
- `workflow_dispatch` до слияния: «To trigger the `workflow_dispatch` event, your workflow must be in the default branch». Поэтому до слияния workflow проверяется через push в ветки (push работает и для workflow, «not merged into the default branch»); `gh workflow run` не использовался.
- Отчёт: три артефакта с `retention-days: 14` (по умолчанию 90, допустимо 1–90): `allure-results` (`if-no-files-found: warn`), `test-reports` — `build/test-results/test` и `build/reports/tests/test` (`error`), `allure-report` (`error`). Загрузка результатов — если шаг тестов выполнился (успех или падение). GitHub Pages: job `deploy-pages` с environment `github-pages` только для `refs/heads/master`, если отчёт сгенерирован и загружен (в том числе при упавших тестах). Сайт будет публичным (https://dumowka.github.io/getting-started-java/), отчёт перезаписывается, истории нет. В настройках нужно включить Pages с Source = GitHub Actions; `configure-pages` с `enablement` не подходит — нужен токен, отличный от `GITHUB_TOKEN`. По решению пользователя включение Pages отложено до слияния.
- Inputs: один `browser_size` (`choice`: `1366x768` по умолчанию, `1920x1080`). Значение передаётся через `env` job (`${{ inputs.browser_size || '1366x768' }}` — при push input пустой) и проверяется `case` первым шагом; при ошибке — `::error::` без вывода значения и `exit 1`. `headless` всегда `true`; `browser` и `baseUrl` не передаются.
- Браузер: предустановленные в образе `ubuntu-24.04` Google Chrome (`.deb`, `/opt/google/chrome`) и ChromeDriver той же сборки (`/usr/local/share/chromedriver-linux64`, симлинк в `/usr/bin`, скрипт `install-google-chrome.sh` образа). Версию на GitHub-hosted runner закрепить нельзя (образ обновляется еженедельно), поэтому версии печатаются в каждом запуске. Chrome for Testing (`browser-actions/setup-chrome`) отклонён: потребовал бы передать пути к браузеру и драйверу (дополнительные свойства), а на Ubuntu 24.04 непривилегированные user namespaces ограничены AppArmor (runner-images #10443), и профиль `chrome` из пакета `apparmor` разрешает их только для `/opt/google/chrome/chrome` — Chrome в другом каталоге, вероятно, не запустит sandbox. Selenium 4.49.0 поддерживает CDP v151–v153 (каталоги `devtools` в теге `selenium-4.49.0`).
- Node.js 24.22.0 (Active LTS «Krypton», 2026-10-14; зависимости Allure требуют максимум `>= 20.19.0`), `npm ci`, `cache: npm`. Проверка результатов перед генерацией обязательна: локально `npx allure generate` на пустом каталоге создаёт отчёт с нулём тестов и exit code 0, на отсутствующем — `No test results directories found…`, exit code 1.
- Java: `actions/setup-java`, `temurin`, `java-version: '21'` (мажор, как в toolchain; patch — в логе). Gradle: `setup-gradle` с `cache-provider: basic` (MIT, поверх `actions/cache`) вместо проприетарного Enhanced Caching по умолчанию; wrapper проверяется автоматически.
- Runner `ubuntu-24.04` (GA, на него указывает `ubuntu-latest`). `timeout-minutes`: 20 для `test`, 10 для `deploy-pages` (по умолчанию 360; деплой Pages сам прерывается через 10 минут). `concurrency`: workflow — `${{ github.workflow }}-${{ github.ref }}`, `cancel-in-progress` везде, кроме `master`; job деплоя — группа `pages`, `cancel-in-progress: false`.
- Permissions: `permissions: {}` на уровне workflow; `test` — `contents: read` (`checkout` с `persist-credentials: false`); `deploy-pages` — `pages: write`, `id-token: write`. Secrets не нужны.
- Внешние действия, разрешённые пользователем: push `phase-5` и трёх временных веток; удаление временных веток — после отдельного подтверждения. Включение Pages отложено. `gh auth refresh -s workflow` не понадобился: push файла workflow приняли учётные данные git из keychain (у токена `gh` scopes `gist, read:org, repo`).
- Локальная проверка: actionlint 1.7.12 из GitHub Releases в scratchpad вне проекта, с проверкой sha256.

Actions (теги — `latest` на 2026-10-20; SHA сверены через REST API с разыменованием annotated-тегов и через `git ls-remote …^{}`):

| Action | Тег | SHA |
|---|---|---|
| actions/checkout | v7.0.1 | `3d3c42e5aac5ba805825da76410c181273ba90b1` |
| actions/setup-java | v6.1.0 | `b843ebf9b913146b38da2eaa7aeddf76074ae1ad` |
| gradle/actions/setup-gradle | v6.4.0 | `3f5f9adaf7d9fecd50b5935e54106014257a94e6` |
| actions/setup-node | v7.1.0 | `949feb2413d6458794dcd2491c4babbbce0c15c1` |
| actions/upload-artifact | v7.0.2 | `cf430e030ddbb5b0abf93d22962f4752f3646cd9` |
| actions/upload-pages-artifact | v5.0.0 | `fc324d3547104276b827a68afc52ff2a11cc49c9` |
| actions/deploy-pages | v5.0.1 | `368f82528645a54fb793d4d04e342629a3f51346` |

`upload-pages-artifact` v5.0.0 внутри использует `actions/upload-artifact@bbbca2ddaa5d8feaa63e36b76fdaad77386f024f` (v7.0.0).

Источники:

- https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows
- https://docs.github.com/en/actions/how-tos/manage-workflow-runs/manually-run-a-workflow
- https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax (inputs: до 25, 65 535 символов; `timeout-minutes` по умолчанию 360; permissions; concurrency)
- https://docs.github.com/en/actions/reference/security/secure-use (промежуточная переменная `env` для недоверенного ввода, pin по полному SHA)
- https://docs.github.com/en/actions/reference/limits (job на GitHub-hosted runner — до 6 часов)
- https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages, …/configuring-a-publishing-source-for-your-github-pages-site, …/github-pages-limits
- https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments
- README и `action.yml` перечисленных actions в указанных тегах; `docs/setup-gradle.md` в `gradle/actions` v6.4.0
- https://github.com/actions/runner-images: README, `images/ubuntu/Ubuntu2404-Readme.md`, `images/ubuntu/scripts/build/install-google-chrome.sh`, issue #10443
- профиль `profiles/apparmor.d/chrome` пакета `apparmor` (Launchpad, `ubuntu/noble`)
- https://github.com/SeleniumHQ/selenium/tree/selenium-4.49.0/java/src/org/openqa/selenium/devtools
- https://nodejs.org/dist/index.json
- https://github.com/rhysd/actionlint/releases/tag/v1.7.12

### Изменения

- `.github/workflows/ui-tests.yml` (новый): job `test` — проверка input, checkout, Java, Gradle, Node.js, `npm ci`, вывод версий (`google-chrome`, `chromedriver`, `java`, `node`, `npx allure`, `./gradlew --version`), `./gradlew testClasses --console=plain`, `./gradlew test -Dselenide.headless=true "-Dselenide.browserSize=$BROWSER_SIZE" --console=plain`, загрузка `allure-results` и `test-reports`, проверка `compgen -G 'build/allure-results/*-result.json'`, `npx allure generate build/allure-results --output build/allure-report --clean`, загрузка `allure-report`, на `master` — `upload-pages-artifact`. Job `deploy-pages` — `deploy-pages` на `master`. Шаги после тестов выполняются по `!cancelled()` и исходу предыдущих шагов, без `continue-on-error` и `|| true`; `defaults.run.shell: bash` (`-eo pipefail`). Комментарии — только теги рядом с SHA.
- Код тестов, Page Object, `build.gradle`, версии зависимостей, `package.json` и `allure.properties` не менялись.

### Проверки

Локально:

- actionlint 1.7.12 (`actionlint_1.7.12_darwin_arm64.tar.gz`): sha256 архива `aba9ced2…6953f` и `actionlint_1.7.12_checksums.txt` `433028cf…a4f5` совпали с digest в API релиза; `shasum -a 256 -c` по `checksums.txt` — OK. `actionlint -verbose .github/workflows/ui-tests.yml`: 0 ошибок, exit code 0. Правила `shellcheck` и `pyflakes` отключены — этих программ нет локально.
- Скрипты шагов в `bash -eo pipefail`: проверка результатов — каталог только с `*-container.json` → exit 1, каталога нет → exit 1, есть `*-result.json` → exit 0; проверка input — `1366x768`, `1920x1080` → 0; пустое значение, `1366x768; id`, значение с переводом строки → exit 1.

В GitHub Actions (все запуски — `push`, runner `ubuntu-24.04`, образ 20261018.339.1; Chrome и ChromeDriver 155.0.8059.84, Temurin 21.0.12.1+1, Node.js 24.22.0, Allure 3.21.0, Gradle 9.8.2; `setup-gradle` подтвердил checksum wrapper `3b8a2577…8e98`). Запуски шли по очереди, следующий — после завершения предыдущего:

| Run id | Ветка | Коммит | Итог |
|---|---|---|---|
| 39396282438 | `phase-5` | коммит фазы с workflow | success, 56 с |
| 39396528519 | `phase-5-ci-assert-fail` | временный: ожидаемый текст `QA.GURU-PHASE5-CONTROLLED-FAIL` | failure, 77 с |
| 39396821847 | `phase-5-ci-compile-error` | временный: удалена `;` в `SearchTests.java:13` | failure, 48 с |
| 39396981175 | `phase-5-ci-no-results` | временный: `allure.results.directory=build/allure-results-phase5-redirected` | failure, 62 с |

- 39396282438: все шаги success, `Upload Pages artifact` и job `deploy-pages` — skipped (не `master`). `Run tests`: BUILD SUCCESSFUL in 12s. Артефакты: `allure-results` (3 `*-result.json`, 4 `*-container.json`), `test-reports` (`tests="3" skipped="0" failures="0" errors="0"`; 3.7 / 1.6 / 1.3 с), `allure-report`. В stderr тестов — только предупреждение CDP: версия 155, ближайшая поддерживаемая — 153.
- 39396528519: `Run tests` — failure: `SearchTests > successfulSearchTest() FAILED`, `com.codeborne.selenide.ex.ElementShould at SearchTests.java:13`, `3 tests completed, 1 failed`, BUILD FAILED, `Process completed with exit code 1`. Загрузки, проверка результатов и генерация — success; job — failure. Артефакты: `allure-results` с 2 вложениями (PNG 149 КБ, HTML 249 КБ), `test-reports` (`failures="1"`), `allure-report`. На скриншоте — обычная выдача (`10.5k results`), признаков HTTP 429 нет: падение контролируемое.
- 39396821847: `Compile tests` — failure (`SearchTests.java:13: error: ';' expected`, BUILD FAILED, exit code 1); `Run tests`, все загрузки, проверка и генерация — skipped; артефактов 0.
- 39396981175: `Compile tests`, `Run tests` (BUILD SUCCESSFUL in 16s) — success; `Upload Allure results` — `##[warning]No files were found with the provided path: build/allure-results. No artifacts will be uploaded.`; `Upload test reports` — success; `Check Allure results` — `##[error]No Allure results found in build/allure-results`, exit code 1; генерация и загрузка отчёта — skipped. Артефакт только `test-reports`; отчёт не создан.
- Неожиданных падений, ошибок окружения (запуск Chrome, Pages) и HTTP 429 не было.
- Временные изменения были только во временных ветках; ветка `phase-5` их не содержит. Временные ветки (локально и в `origin`) ожидают подтверждения удаления.

### Ограничения и непроверенное

- Не проверены, потому что требуют workflow в `master`: ручной запуск `workflow_dispatch`, input `browser_size` через UI или `gh workflow run` (включая `1920x1080` в CI), запуск на `master` с загрузкой `upload-pages-artifact`, job `deploy-pages`, передача output `pages-artifact` при упавшем job `test`, environment `github-pages` и его правила. Pages ещё не включён; без этого деплой на `master` упадёт.
- `concurrency` (отмена прогона при новом push в ту же ветку, очередь на `master`) не проверялась: запуски шли по одному.
- Версии Chrome и ChromeDriver определяются образом runner и меняются с его обновлением (README образа на момент этапа A показывал 155.0.8059.39, в запусках — 155.0.8059.84). Selenium 4.49.0 выдаёт предупреждение CDP 155/153; на тесты не влияло.
- Кэш Gradle (`basic`) на ветках работает только на чтение: по умолчанию пишется только с default branch, поэтому во всех запусках фазы кэша не было (`0 restored, 0 saved`). Кэш npm сохранён в запуске `phase-5`.
- В CI тесты шли 1.3–3.7 с и без HTTP 429, но анонимный поиск github.com с IP датацентра может получить 429 или замедлиться; однократные запуски стабильности не гарантируют.
- Run-скрипты не проверены shellcheck (его нет локально).
- Без проверки результатов при пустом каталоге Allure 3.21.0 создал бы отчёт с нулём тестов (проверено локально); в CI проверялся только случай отсутствующего каталога, пустой — локально.
- Проверка input работает и при push (fallback `1366x768`); ветка `case` с ошибкой в CI не запускалась.

## Фаза 6 — Telegram

Статус: не реализована. Реализация начинается после согласования пунктов ниже; реальная отправка — только с разрешения пользователя. Все пункты остаются открытыми до подтверждённых результатов.

### Перед реализацией согласовать

- [ ] Нужны ли уведомления из GitHub Actions и Jenkins и для каких исходов.
- [ ] Имена secrets/credentials, поведение при отсутствии секретов и ошибке Telegram.
- [ ] Разрешение на реальную отправку сообщения.

### Реализация и проверка

- [ ] Использовать secrets окружения без хардкода и логирования.
- [ ] Брать passed/failed/skipped из проверенного формата JUnit XML; не опираться на непроверенные внутренние файлы Allure-отчёта.
- [ ] Отличать сбой сборки до запуска тестов от успешного запуска с нулём тестов.
- [ ] Включить проект, CI, branch/commit, статус тестов, статистику и ссылку на запуск.
- [ ] Проверять HTTP-ответ и результат Telegram API; не маскировать исходное падение ошибкой уведомления.
- [ ] Проверить согласованные случаи без реальной отправки, затем выполнить разрешённую отправку; создать коммит.

## Фаза 7 — Jenkins

Статус: не реализована. Реализация начинается только после отдельного согласования окружения Jenkins. Наличие Jenkinsfile не означает проверку pipeline; запуск и публикация отчёта подтверждаются на реальном агенте либо остаются явно непроверенными.

### Перед реализацией уточнить

- [ ] Где находятся Jenkins и агент, доступ, ОС, label и доступные инструменты.
- [ ] Доступность Java, браузеров, драйверов и генератора Allure, выбранного в фазе 4.
- [ ] Параметры запуска, имена Jenkins Credentials и Telegram-уведомления.
- [ ] Способ генерации и публикации Allure-отчёта (CLI, как в GitHub Actions, или Allure Jenkins plugin).

### Реализация и проверка

- [ ] Добавить Jenkinsfile с теми же командами тестов и генерации отчёта, что локально и в GitHub Actions.
- [ ] Публиковать JUnit XML, сохранять результаты, вложения и отчёт при падениях.
- [ ] Проверить выбранный способ генерации отчёта на реальном агенте; не предполагать наличие плагина или CLI.
- [ ] При отсутствии совместимости согласовать альтернативную генерацию и хранение отчёта.
- [ ] Не устанавливать плагины и не менять Jenkins глобально без разрешения.
- [ ] Разделить проверку Jenkinsfile и реальный запуск Jenkins; создать коммит.

## Фаза 8 — Review

Статус: не реализована.

- Согласовать объём review и допустимость изменений.
- Проверить реализованные фазы на чистом checkout: wrapper и toolchain, сборку, тесты, передачу свойств, lifecycle браузера, Allure-отчёт.
- Проверить CI по реальным запускам: успешный и контролируемые падения, закрепление версий, permissions, публикацию отчёта.
- Проверить отсутствие секретов и generated-файлов в git.
- Зафиксировать findings, риски и непроверенное.

## Фаза 9 — Доработка findings финального review

Статус: не реализована. Объём определяется findings фазы 8 и согласуется с пользователем перед реализацией.

- Согласовать, какие findings исправлять, а какие принять как риск.
- Исправлять по одной задаче с проверкой.
- Подтверждать исправления локально и, если они касаются CI, в реальном GitHub Actions.
- Обновить план и создать коммиты.

## Базовые команды проверки

Команды выполняются из корня Git-репозитория. `./gradlew` появляется в фазе 1, команды Allure — в фазе 4 (нужен Node.js; `npm ci` ставит закреплённую версию из `package-lock.json`). Успешный запуск не гарантирует доступность внешнего сайта при следующем запуске.

```bash
./gradlew --version
./gradlew test --rerun-tasks --console=plain
./gradlew test -Dselenide.headless=true -Dselenide.browserSize=1920x1080 --rerun-tasks --console=plain
npm ci
npx allure --version
npx allure generate build/allure-results --output build/allure-report --clean
npx allure open build/allure-report --host 127.0.0.1
git diff --check
```

При изменении только этого файла достаточно проверить содержимое Markdown, актуальность статусов, whitespace и состав коммита; UI-тесты ради изменения плана запускать не требуется.

## Критерий завершения фазы

1. Выполнен именно согласованный объём.
2. Проверки действительно запущены и результаты зафиксированы.
3. Временная диагностика удалена, ограничения и блокеры перечислены.
4. Этот план обновлён без превращения предположений в подтверждённые результаты.
5. Свои изменения закоммичены; чужой WIP не затронут.
6. Работа остановлена до подтверждения следующей фазы.
