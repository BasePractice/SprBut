# Модуль 16 — Конфигурация

[Слайды 128–138, СХЕМА 10 (слайд 138)](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p138) ·
[← к списку модулей](../README.md)

## Что в презентации

> Конфигурация в коде. Конфигурация в файле. Приоритеты конфигураций:
> значения по умолчанию в коде → значения в файле (yaml/properties) →
> переменные окружения → системные (`-D`) → аргументы командной строки (`--op=val`).
> `@Value` и `@ConfigurationProperties`. Профили: `application-{profile}.yaml`.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`ServerProperties`](src/main/java/ru/sprbut/m16/ServerProperties.java) | 137 | `@ConfigurationProperties`: типизация, группировка, вложенность, проверка |
| [`ValueBasedConfig`](src/main/java/ru/sprbut/m16/ValueBasedConfig.java) | 137 | `@Value`: проще в записи, дороже в поддержке |
| [`ConfigurationApp`](src/main/java/ru/sprbut/m16/ConfigurationApp.java) | — | `@ConfigurationPropertiesScan` вместо ручной регистрации |
| [`application.yaml`](src/main/resources/application.yaml) | 138 | Базовые значения и профиль `prod` рядом |

## Расширенный пример

[`ConfigurationOrigin`](src/main/java/ru/sprbut/m16/extended/ConfigurationOrigin.java) —
инструмент, отвечающий на вопрос «откуда взялось это значение».

Приоритеты со слайда перестают быть списком, который надо помнить: `Environment`
показывает весь стек источников целиком и говорит, какой из них победил.
Это первое, что стоит сделать, когда приложение в проде ведёт себя не так,
как на машине разработчика.

## Ключевые выводы

* Приоритет источников фиксирован и одинаков для всех приложений Boot.
  Спорить с ним бесполезно — можно только знать.
* `@ConfigurationProperties` с привязкой через конструктор даёт неизменяемые
  настройки: значения приезжают один раз и подменить их нельзя.
* `@Value` не умеет ни группировать, ни валидировать, а опечатка в имени
  свойства обнаруживается только в рантайме.
* Профиль — не «режим приложения», а всего лишь ещё один слой в том же стеке.

## Запуск

```bash
mvn -pl 16-configuration test
```
