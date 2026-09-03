# Модуль 19 — Автоконфигурация

[Слайды 191–196, СХЕМА 12 (слайд 197)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p185) ·
[← к списку модулей](../README.md)

## Что в презентации

> Стартеры: `spring-boot-starter-*`. `AutoConfiguration.imports` в `META-INF/spring`.
> `@ConditionalOnClass`, `@ConditionalOnMissingBean`. Свой бин переопределяет
> автоконфигурацию. Отчёт об условиях: запуск с `--debug`.

Модуль устроен как настоящий стартер, только маленький.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`Greeter`](src/main/java/ru/sprbut/m19/greeter/Greeter.java) | — | Контракт «библиотеки», ради которой пишется стартер |
| [`GreeterProperties`](src/main/java/ru/sprbut/m19/greeter/GreeterProperties.java) | — | Префикс по имени стартера — соглашение всей экосистемы |
| [`GreeterAutoConfiguration`](src/main/java/ru/sprbut/m19/autoconfigure/GreeterAutoConfiguration.java) | 174–177 | Настоящая автоконфигурация целиком: три идеи и ничего больше |
| [`AutoConfiguration.imports`](src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports) | 175 | Строчка в файле вместо сканирования пакетов |

## Расширенный пример

[`ConditionReport`](src/main/java/ru/sprbut/m19/extended/ConditionReport.java) —
программный доступ к отчёту об условиях, тому самому, что Boot печатает
по флагу `--debug`.

Отчёт отвечает на два вопроса, которые иначе решаются гаданием: почему бин
**не** появился и почему появился не тот. Оба ответа Boot знает точно
и хранит их в `ConditionEvaluationReport`.

## Ключевые выводы

* Стартер — это не магия, а файл со списком классов плюс аннотации условий.
* Автоконфигурации обрабатываются **последними**, и только поэтому
  `@ConditionalOnMissingBean` работает: к моменту проверки бины приложения
  уже зарегистрированы. В обычной `@Configuration` тот же приём ломается —
  разобрано в [модуле 27](../27-capstone/README.md).
* `@ConditionalOnClass` проверяет classpath, не загружая класс: иначе
  отсутствующая зависимость роняла бы старт вместо тихого пропуска.
* Свой бин побеждает автоконфигурацию по умолчанию — это её главный контракт.

## Запуск

```bash
mvn -pl 19-autoconfiguration test
```
