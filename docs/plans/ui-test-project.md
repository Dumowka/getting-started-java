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

## Текущее состояние

- Фаза 0 завершена 2026-10-20: проверки выполнены, baseline 1/1 получен (Gradle 9.8.1 из локального кэша, Java 21), решения по параметрам, Java, wrapper и Page Object согласованы. Детали и ограничения — в разделе фазы 0.
- Фаза 1 реализована 2026-10-20 в ветке `phase-1` (один коммит; по решению пользователя запушена в `origin` и открыт PR в `master`, не влита): Gradle Wrapper 9.8.2, Java 21 toolchain, TestBase, передача четырёх свойств `selenide.*`; финальные запуски 1/1 с defaults и с переопределением. Детали — в разделе фазы 1.
- Фазы 2–9 не реализованы: дополнительных тестов, Page Object, Allure и CI нет.

Порядок дальнейшей работы: фазы выполняются по одной — согласование, реализация, проверка, отдельный коммит. Следующая — фаза 2, после подтверждения пользователя.

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

Статус: реализована 2026-10-20 в ветке `phase-1`, созданной от `master` после fast-forward до `3e7e2f8` (merge PR #1, фаза 0). Один коммит. По решению пользователя 2026-10-20 ветка запушена в `origin/phase-1` и для неё открыт PR в `master`; в `master` не влита. Фаза 2 ожидает подтверждения пользователя.

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

Статус: не реализована.

### Этап A — исследование и согласование, без изменения кода

- Предложить два дополнительных независимых сценария и получить подтверждение пользователя.
- Проверить доступность сценариев в реальном UI.
- Зафиксировать цели, предусловия, данные, assertions, локаторы и риски каждого сценария.
- Подтвердить, что сценарии не зависят от неконтролируемых данных, либо явно зафиксировать этот риск.

### Этап B — реализация и проверка

- Добавить два независимых теста в SearchTests.
- Сохранить исходный сценарий: qa.guru и проверку текста QA.GURU.
- Использовать TestBase, относительный /search, ожидания Selenide и содержательные assertions.
- Проверить каждый новый тест отдельно, весь набор с defaults и в headless.
- Установить причину каждого наблюдённого падения (например, HTTP 429 от GitHub), не маскируя его ретраями.
- Удалить временную диагностику и создать отдельный коммит.

## Фаза 3 — Page Object

Статус: не реализована.

- Подтвердить применимость согласованного стиля к фактическим сценариям.
- Перенести тесты и TestBase в пакет `tests`, страницы разместить в пакете `pages`.
- Перенести взаимодействия и локаторы в страницы, проверки — в методы страниц.
- Добавить BasePage и компоненты сразу, по решению фазы 0.
- Сохранить локаторы, тестовые данные, assertions и порядок проверок; проверить каждый тест отдельно, весь набор с defaults и в headless; закоммитить изменения.

## Фаза 4 — Allure Report 3

Статус: не реализована.

### Перед реализацией согласовать

- Генератор отчёта и способ его установки локально и в CI.
- JVM-адаптеры Allure и их версии.
- Шаги в отчёте (`@Step` на методах страниц, технические шаги Selenide) и вложения при падении.
- Расположение результатов и отчёта; оба не должны попадать в git.

### Реализация и проверка

- Разделить JVM-адаптеры Allure и CLI-генератор отчёта.
- Проверить совместимость выбранных версий с Java 21, JUnit 6 и Selenide по документации; не понижать Java, JUnit и Selenide без согласования.
- Выбрать и зафиксировать проверенные версии.
- Проверить актуальную документацию и встроенный --help генератора.
- Сгенерировать и открыть отчёт, проверить диагностику контролируемого падения.
- Восстановить успешное состояние, исключить generated artifacts из git и создать коммит.

## Фаза 5 — GitHub Actions

Статус: не реализована.

### Перед реализацией согласовать

- События запуска workflow.
- Публикацию отчёта: артефакты, GitHub Pages, условия публикации.
- Inputs workflow и их валидацию.
- Установку браузера и драйвера в CI и их версии.
- Репозиторий для реального запуска CI.
- Нужны ли secrets.

### Реализация и проверка

- Добавить workflow с явной передачей `selenide.headless=true`.
- Закрепить версии actions и инструментов; задать минимальные permissions.
- Компилировать тесты отдельно от запуска; сохранять результаты и отчёт при падении тестов; не маскировать exit code тестов.
- Проверить workflow локально (например, actionlint).
- Проверить в реальном GitHub Actions: успешный запуск, контролируемое падение assertion, ошибку компиляции и отсутствие результатов.
- Удалить временные ветки и диагностику, создать коммит.

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

Команды выполняются из корня Git-репозитория. `./gradlew` появляется в фазе 1; команды Allure добавить после согласования генератора в фазе 4. Успешный запуск не гарантирует доступность внешнего сайта при следующем запуске.

```bash
./gradlew --version
./gradlew test --rerun-tasks --console=plain
./gradlew test -Dselenide.headless=true -Dselenide.browserSize=1920x1080 --rerun-tasks --console=plain
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
