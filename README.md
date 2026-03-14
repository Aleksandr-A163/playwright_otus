# OTUS Playwright UI tests

Проект содержит UI автотесты на Playwright Java + JUnit 5 + Guice.

## Что реализовано

- DI через Guice
- JUnit 5 extension для создания `Playwright`, `Browser`, `BrowserContext`, `Page`
- red highlight для кликов и drag через JS
- tracing Playwright на каждом тесте
- разделение структуры: в `src/test` только тесты, в `src/main` — page objects и инфраструктура
- Checkstyle и SpotBugs

## Структура

```text
src/main/java/ru/otus/
  annotations/
  datamodule/
  exceptions/
  extensions/
  modules/
  pages/
    components/
  utils/

src/test/java/ru/otus/tests/
  TeachersUiTest
  CatalogCoursesFilterTest
  SubscriptionTest
```

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

## URL настройки

`TeachersUiTest` использует:

```bash
-DbaseUrl=https://otus.ru/lessons/clickhouse/
```

`CatalogCoursesFilterTest` и `SubscriptionTest` используют:

```bash
-DsiteUrl=https://otus.ru
```

## Трейсы

После выполнения тестов zip-файлы с трассировками будут лежать в корне проекта в папке:

```text
traces/
```
