# OTUS Playwright UI tests

Проект содержит UI автотест на Playwright Java + JUnit 5 + Guice без `@UsePlaywright`.

## Что реализовано

- DI через Guice
- JUnit 5 extension вместо `@UsePlaywright`
- red highlight для каждого клика и drag через JS
- трассировка Playwright на каждом тесте
- Checkstyle и SpotBugs
- запуск из консоли

## Структура

- `PlaywrightExtension` — создание `Playwright`, `Browser`, `BrowserContext`, `Page`, старт/стоп tracing
- `UiTestModule` — Guice module
- `UiActions` — клик и drag c подсветкой
- `OtusClickHousePage`, `TeachersSection`, `TeacherPopup` — page objects
- `TeachersUiTest` — основной UI тест по сценарию

## Запуск

```bash
./gradlew clean test
```

Headless режим можно отключить:

```bash
./gradlew clean test -Dheadless=false
```

Выбор браузера:

```bash
./gradlew clean test -Dbrowser=chromium
./gradlew clean test -Dbrowser=firefox
./gradlew clean test -Dbrowser=webkit
```

## Трейсы

После выполнения тестов zip-файлы с трассировками будут лежать в корне проекта в папке:

```text
traces/
```

Это соответствует требованию хранить архивы с трейсами в корне репозитория.

## Что можно усилить дальше

- вынести конфиг в отдельный properties/env слой
- добавить base component для карточек преподавателей
- включить retry и screenshots on failure
- добавить GitHub Actions или Jenkins pipeline
