# Модуль 03 — Reflection API

[Слайды 22–28, СХЕМА 1 (слайд 29)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p21) ·
[← к списку модулей](../README.md)

## Что в презентации

> Пакет `java.lang.reflect`: **Field**, **Method**, **Constructor**, **Modifier**, **Class**.
>
> СХЕМА 1 (слайд 27) — карта Reflection API: `Class` в центре, остальные классы вокруг.

Модуль повторяет эту карту один-к-одному: по классу-примеру на каждый узел.

## Примеры

| Класс | Узел карты | Что показывает |
|---|---|---|
| [`ClassApi`](src/main/java/ru/sprbut/m03/ClassApi.java) | **Class** (центр) | Из `Class` достаются все остальные узлы; категории типов, `isAssignableFrom`, record-компоненты, `Array.newInstance` |
| [`TypeKind`](src/main/java/ru/sprbut/m03/TypeKind.java) | Class | Категория типа одним словом — и почему порядок проверок обязателен |
| [`FieldType`](src/main/java/ru/sprbut/m03/FieldType.java) | Field | `getType()` vs `getGenericType()`, аргументы дженерика |
| [`Boxed`](src/main/java/ru/sprbut/m03/Boxed.java) | Field | Примитив и его обёртка: без замены проверка типов всегда ложна |
| [`MethodSignature`](src/main/java/ru/sprbut/m03/MethodSignature.java) | Method | Сигнатура, `Parameter` с реальными именами (`-parameters`), varargs, bridge- и default-методы |
| [`Constructors`](src/main/java/ru/sprbut/m03/Constructors.java) | Constructor | Подбор конструктора по типам аргументов — точка входа любого IoC-контейнера |
| [`NewInstance`](src/main/java/ru/sprbut/m03/NewInstance.java) | Constructor | Создание объекта и три разных причины отказа |
| [`Flags`](src/main/java/ru/sprbut/m03/Flags.java) | Modifier | Модификаторы как битовая маска; package-private — отсутствие битов |
| [`Order`](src/main/java/ru/sprbut/m03/model/Order.java) | — | Подопытный класс: 4 конструктора, дженерики, varargs, `throws`, `volatile`, `synchronized` |

## Расширенный пример

[`Command`](src/main/java/ru/sprbut/m03/extended/Command.java) — мини-движок команд,
который задействует **все узлы карты сразу**:

```java
new Command("ru.sprbut.m03.model.Order(A-1,10)#addLines(1,2,3)").invocation();
// Invocation[type=Order, constructor=Order(String, BigDecimal),
//            signature=BigDecimal addLines(BigDecimal[]), result=16]
```

`Class.forName` загружает тип → [`ChosenConstructor`](src/main/java/ru/sprbut/m03/extended/ChosenConstructor.java)
выбирается по арности и типам → `Parameter` даёт типы, по которым
[`Argument`](src/main/java/ru/sprbut/m03/extended/Argument.java) конвертирует строки →
[`ChosenMethod`](src/main/java/ru/sprbut/m03/extended/ChosenMethod.java) находится
с учётом varargs → `Array.newInstance` упаковывает хвост аргументов →
[`AccessRank`](src/main/java/ru/sprbut/m03/extended/AccessRank.java) предпочитает
доступное недоступному.

Это упрощённая модель того, как работают `spring-shell`, JMX-операции и маршрутизация
запросов в Spring MVC.

## Ключевые выводы

* `Class` — единственная точка входа; всё остальное API добывается из него.
* Дженерики **не** полностью стёрты: параметры типа полей и сигнатур лежат в атрибуте
  `Signature` класс-файла и достаются через `getGenericType()`.
* varargs в байткоде — обычный параметр-массив плюс флаг `isVarArgs`.
* Имена параметров доступны только если код скомпилирован с `-parameters`
  (включено в корневом `pom.xml`); иначе будут `arg0`, `arg1`.
* Bridge-методы генерирует компилятор — при сканировании их надо отфильтровывать,
  иначе один метод «находится» дважды.
* Массив нельзя создать через `Constructor`: для этого есть `java.lang.reflect.Array`.

## Запуск

```bash
mvn -pl 03-reflection-api test
```
