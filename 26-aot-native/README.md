# Модуль 26 — AOT, native image, версии

[Слайды 267–277, СХЕМА 24 (слайд 278)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p209) ·
[← к списку модулей](../README.md)

## Что в презентации

> GraalVM native-image: рефлексии почти нет. Spring AOT генерирует код при сборке.
> `RuntimeHints` для остатков рефлексии. Быстрый старт, меньше памяти.
> Цена: долгая сборка, меньше динамики.
>
> Spring Boot 3.x, минимум Java 17. `javax.*` переехал в `jakarta.*`.
> `jakarta.annotation.PostConstruct`. `javax.annotation.processing` не переименован.

Практикум собран на Spring Boot 4.1 и Java 25: минимум для Boot 4 — Java 17,
переезд `javax.*` → `jakarta.*` остался в прошлом, а Jackson поднялся до третьей
версии и переехал из `com.fasterxml.jackson` в `tools.jackson`.

Это единственный модуль курса, где рефлексия из решения превращается в проблему.

## Примеры

| Класс | Что показывает |
|---|---|
| [`PluginByName`](src/main/java/ru/sprbut/m26/reflection/PluginByName.java) | `Class.forName` + `newInstance` — ровно то, что не переживает native image |
| [`CsvPlugin`](src/main/java/ru/sprbut/m26/reflection/CsvPlugin.java) | Класс, объявленный в подсказках: доживёт до рантайма образа |
| [`JsonPlugin`](src/main/java/ru/sprbut/m26/reflection/JsonPlugin.java) | Класс, который забыли объявить: на JVM неотличим от предыдущего |
| [`PluginHints`](src/main/java/ru/sprbut/m26/hints/PluginHints.java) | `RuntimeHintsRegistrar`: обещание сборщику про классы и ресурсы |
| [`PluginConfig`](src/main/java/ru/sprbut/m26/hints/PluginConfig.java) | `@ImportRuntimeHints` — одна строчка разницы между рабочим образом и падающим |
| [`Migration`](src/main/java/ru/sprbut/m26/versions/Migration.java) | Правило `javax.*` → `jakarta.*` и его единственное исключение |
| [`Cache`](src/main/java/ru/sprbut/m26/versions/Cache.java) | `jakarta.annotation.PostConstruct` / `@PreDestroy` в Boot 3 |

## Расширенный пример

[`NativeReadiness`](src/main/java/ru/sprbut/m26/extended/NativeReadiness.java) — аудит
готовности к native image: сверяет классы, которые создаются рефлексией, с тем,
что реально попало в `RuntimeHints`.

```java
RuntimeHints hints = new RuntimeHints();
new PluginHints().registerHints(hints, classLoader);

new NativeReadiness(hints).gaps(List.of(CsvPlugin.class, JsonPlugin.class));
// [ru.sprbut.m26.reflection.JsonPlugin]
```

Ценность приёма в том, **что именно он ловит**. Забытый класс ведёт себя безупречно
на JVM, сборка native тоже проходит успешно — падает уже готовый образ, в рантайме,
у пользователя. Обычный тест на поведение такую дыру не увидит никогда: проверять
надо подсказки, а не результат. Так же устроены тесты подсказок в самом Spring
(`RuntimeHintsPredicates`).

## Ключевые выводы

* Native image собирает **граф достижимости** от точки входа. Класс, названный
  строкой в конфиге, из этого графа не виден и в образ не попадает.
* `RuntimeHints` — не оптимизация, а объявление намерений. Забытая подсказка
  не ломает ни один тест и ни одну сборку: она ломает рантайм.
* Регистрации класса мало — `MemberCategory` решает, попадут ли в образ
  конструкторы, методы и поля.
* Spring AOT переносит работу контейнера со старта на сборку: разбор аннотаций
  и создание определений бинов превращаются в сгенерированный Java-код.
* `javax.*` → `jakarta.*` переехало то, что Oracle передал фонду Eclipse.
  `javax.annotation.processing` принадлежит JDK и остался на месте.
* Аннотация из «неправильного» пакета не вызывает ошибки — она просто ничего
  не значит. Не переехавший `@PostConstruct` молча не вызовется.

## Запуск

```bash
mvn -pl 26-aot-native test
```

Полноценная сборка образа требует GraalVM и в тестах курса не выполняется:

```bash
mvn -pl 26-aot-native -Pnative native:compile
```
