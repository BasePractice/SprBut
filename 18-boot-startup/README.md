# Модуль 18 — Запуск Spring Boot

[Слайды 175–188, СХЕМА 11 (слайд 189)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p169) ·
[← к списку модулей](../README.md)

## Что в презентации

> `BootstrapContext` → `ApplicationContext` → `SpringApplicationRunListener.starting` →
> `ApplicationStartingEvent` → `ApplicationEnvironmentPreparedEvent` →
> `ApplicationContextInitializer` → `ApplicationContextInitializedEvent` →
> регистрация `ApplicationListeners` → `ApplicationPreparedEvent` →
> `refresh` → `ApplicationStartedEvent` → `ApplicationRunner` → `ApplicationReadyEvent`.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`StartupApp`](src/main/java/ru/sprbut/m18/StartupApp.java) | 156–157 | `run()` — не «запустить main», а конкретная последовательность шагов |
| [`StartupListeners`](src/main/java/ru/sprbut/m18/StartupListeners.java) | 158–172 | Чем события различаются: **что к этому моменту уже готово** |
| [`StartupHooks`](src/main/java/ru/sprbut/m18/StartupHooks.java) | 161, 170 | Три хука между событиями, у каждого своё место и задача |
| [`FailingConfig`](src/main/java/ru/sprbut/failing/FailingConfig.java) | — | `ApplicationFailedEvent` вместо `ApplicationReadyEvent` |

## Расширенный пример

[`StartupTimeline`](src/main/java/ru/sprbut/m18/extended/StartupTimeline.java) —
восстановление последовательности запуска, прямая реализация СХЕМЫ 11.

Практическая ценность — ответ на вопрос «куда вешать свой код». Он выводится
не из списка событий, а из того, что к моменту каждого события уже существует:
на `ApplicationEnvironmentPreparedEvent` есть конфигурация, но нет бинов;
на `ApplicationPreparedEvent` есть определения, но бины ещё не созданы;
на `ApplicationReadyEvent` готово всё.

## Ключевые выводы

* Между «приложение стартовало» и «приложение готово» лежит десяток шагов,
  и код, повешенный не туда, работает с полусобранным контекстом.
* `refresh()` — момент, внутри которого происходит вся работа контейнера
  из модуля 14: `BeanFactoryPostProcessor`, `@PostConstruct`, прокси, `SmartLifecycle`.
* Слушатель, зарегистрированный слишком поздно, не получит ранние события —
  их просто некому будет доставить.
* `ApplicationRunner` — последнее место, где ещё можно вмешаться до готовности.

## Запуск

```bash
mvn -pl 18-boot-startup test
```
