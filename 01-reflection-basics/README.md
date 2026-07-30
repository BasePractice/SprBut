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
| [`ClassMetadata`](src/main/java/ru/sprbut/m01/ClassMetadata.java) | 3–5 | Имена, иерархия наследования, интерфейсы, признаки инстанцируемости |
| [`ClassByName`](src/main/java/ru/sprbut/m01/ClassByName.java) | 4 | `Class.forName()` — связь через строку, невидимая компилятору |
| [`Members`](src/main/java/ru/sprbut/m01/Members.java) | 6 | `getDeclaredFields()` видит `private`, `getFields()` — только `public` |
| [`Modifiers`](src/main/java/ru/sprbut/m01/Modifiers.java) | 6 | `getModifiers()` как битовая маска |
| [`ObjectField`](src/main/java/ru/sprbut/m01/ObjectField.java) | 7 | `setAccessible(true)`: чтение и запись `private` и `private final` полей |
| [`StaticField`](src/main/java/ru/sprbut/m01/StaticField.java) | 7 | Статическое поле читается через `get(null)` |
| [`ObjectMethod`](src/main/java/ru/sprbut/m01/ObjectMethod.java) | 8 | Вызов `private`-метода и разворачивание `InvocationTargetException` |
| [`StaticMethod`](src/main/java/ru/sprbut/m01/StaticMethod.java) | 8 | Вызов статического метода без экземпляра |
| [`Annotations`](src/main/java/ru/sprbut/m01/Annotations.java) | 9 | `AnnotatedElement`, `getAnnotation`, невидимость `RetentionPolicy.SOURCE` |
| [`Declared`](src/main/java/ru/sprbut/m01/Declared.java) | — | Подъём по иерархии: цикл, написанный в каждом фреймворке |
| [`Account`](src/main/java/ru/sprbut/m01/model/Account.java) | — | Подопытный класс: поля и методы всех уровней доступа |

## Расширенный пример

[`Json`](src/main/java/ru/sprbut/m01/extended/Json.java) — JSON-сериализатор,
написанный **целиком** на рефлексии. Он объединяет все пять пунктов слайда сразу:
поднимается по иерархии классов, фильтрует поля по модификаторам
([`SerializableFields`](src/main/java/ru/sprbut/m01/extended/SerializableFields.java)),
читает значения `private`-полей через `setAccessible(true)` и управляется аннотациями
`@JsonProperty` / `@JsonIgnore`
([`PropertyName`](src/main/java/ru/sprbut/m01/extended/PropertyName.java)).

Это упрощённая модель того, как устроены Jackson, Gson и биндинг Spring: **поведение
задаётся метаданными, а не написанным вручную кодом**.

```java
record Person(String name, Address address) {}

new Json(new Person("Пётр", new Address("Москва"))).text();
// {"name":"Пётр","address":{"city":"Москва"}}
```

Вложенные объекты сериализует тот же класс: рекурсия здесь выражена композицией
объектов, а не отдельным методом обхода.

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
