# Модуль 01 — Reflection и метаданные в runtime

[Слайды 3–10](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p3) ·
[← к списку модулей](../README.md)

## Что в презентации

> Reflection — механизм работы с метаданными объектов в runtime. Используется многими
> фреймворками. Позволяет узнать имя класса объекта; определить модификаторы доступа
> полей и методов; получить и задать значение полей, в том числе `private`; вызвать
> методы объекта, в том числе `private`; получать аннотации.

Каждый пункт этого списка — отдельный класс-пример в модуле.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`ClassMetadata`](src/main/java/ru/sprbut/m01/ClassMetadata.java) | 3–5 | Три способа получить `Class`, имена, иерархия наследования, `Class.forName()` |
| [`ModifierInspector`](src/main/java/ru/sprbut/m01/ModifierInspector.java) | 6 | `getModifiers()` как битовая маска, `getDeclaredFields()` vs `getFields()` |
| [`FieldAccessor`](src/main/java/ru/sprbut/m01/FieldAccessor.java) | 7 | `setAccessible(true)`: чтение и запись `private` и `private final` полей |
| [`MethodInvoker`](src/main/java/ru/sprbut/m01/MethodInvoker.java) | 8 | Вызов `private` и `static` методов, разворачивание `InvocationTargetException` |
| [`AnnotationReader`](src/main/java/ru/sprbut/m01/AnnotationReader.java) | 9 | `AnnotatedElement`, `getAnnotation`, невидимость `RetentionPolicy.SOURCE` |
| [`Account`](src/main/java/ru/sprbut/m01/model/Account.java) | — | Подопытный класс: поля и методы всех уровней доступа |

## Расширенный пример

[`ReflectiveJsonWriter`](src/main/java/ru/sprbut/m01/extended/ReflectiveJsonWriter.java) —
JSON-сериализатор, написанный **целиком** на рефлексии. Он объединяет все пять пунктов
слайда сразу: поднимается по иерархии классов, фильтрует поля по модификаторам
(`static`, `transient`), читает значения `private`-полей через `setAccessible(true)`
и управляется аннотациями `@JsonProperty` / `@JsonIgnore`.

Это упрощённая модель того, как устроены Jackson, Gson и биндинг Spring: **поведение
задаётся метаданными, а не написанным вручную кодом**.

```java
record Person(String name, Address address) {}

ReflectiveJsonWriter.write(new Person("Пётр", new Address("Москва")));
// {"name":"Пётр","address":{"city":"Москва"}}
```

## Ключевые выводы

* `Class` — единственный объект на загруженный класс: `X.class == obj.getClass()`.
* `getDeclaredXxx()` видит `private`, но только в самом классе; `getXxx()` — только
  `public`, зато с родителями. Обход иерархии почти всегда пишется руками.
* `setAccessible(true)` снимает проверку доступа — на этом стоит внедрение
  зависимостей в поля и заполнение сущностей ORM.
* Исключение из вызванного метода приходит завёрнутым в `InvocationTargetException`;
  реальную причину надо доставать через `getCause()`.
* Аннотация видна в runtime **только** с `@Retention(RUNTIME)`.

## Запуск

```bash
mvn -pl 01-reflection-basics test
```
