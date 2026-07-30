# Модуль 03 — Reflection API

[Слайды 20–27, СХЕМА 1 (слайд 27)](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p27) ·
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
| [`FieldApi`](src/main/java/ru/sprbut/m03/FieldApi.java) | Field | `getType()` vs `getGenericType()`, аргументы дженерика, примитивы и обёртки |
| [`MethodApi`](src/main/java/ru/sprbut/m03/MethodApi.java) | Method | Сигнатура, `Parameter` с реальными именами (`-parameters`), varargs, bridge- и default-методы |
| [`ConstructorApi`](src/main/java/ru/sprbut/m03/ConstructorApi.java) | Constructor | Подбор конструктора по типам аргументов — точка входа любого IoC-контейнера |
| [`ModifierApi`](src/main/java/ru/sprbut/m03/ModifierApi.java) | Modifier | Модификаторы как битовая маска; package-private — отсутствие битов |
| [`Order`](src/main/java/ru/sprbut/m03/model/Order.java) | — | Подопытный класс: 4 конструктора, дженерики, varargs, `throws`, `volatile`, `synchronized` |

## Расширенный пример

[`ReflectiveCommandRunner`](src/main/java/ru/sprbut/m03/extended/ReflectiveCommandRunner.java) —
мини-движок команд, который задействует **все узлы карты сразу**:

```java
ReflectiveCommandRunner.run("ru.sprbut.m03.model.Order(A-1,10)#addLines(1,2,3)");
// Invocation[type=Order, constructorUsed=Order(String, BigDecimal),
//            methodSignature=BigDecimal addLines(BigDecimal[]), result=16]
```

`Class.forName` загружает тип → `Constructor` выбирается по арности и типам →
`Parameter` даёт типы, по которым [`ArgumentConverter`](src/main/java/ru/sprbut/m03/extended/ArgumentConverter.java)
конвертирует строки → `Method` находится с учётом varargs → `Array.newInstance`
упаковывает хвост аргументов → `Modifier` отсеивает недоступное.

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
