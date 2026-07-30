# Модуль 04 — Reflection: детали

[Слайды 29–36, СХЕМА 2 (слайд 36)](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p36) ·
[← к списку модулей](../README.md)

## Что в презентации

> `Class`: `X.class`, `obj.getClass()`, `Class.forName()` · `setAccessible` и JPMS:
> нужен `--add-opens` · Стирание типов: `getGenericType`, `ParameterizedType` ·
> Рефлексия медленнее прямого вызова · Быстрее: `MethodHandles`, `VarHandle` ·
> **Proxy и InvocationHandler — основа Spring AOP** · `Parameter`, `Executable`,
> `AnnotatedElement`, `Array`.
>
> СХЕМА 2 (слайд 36) — прокси: вызов → Proxy → InvocationHandler → цель.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`ClassLoading`](src/main/java/ru/sprbut/m04/ClassLoading.java) | 30 | Три способа получить `Class`; `forName` с `initialize=false`; `int.class != Integer.class`; bootstrap-загрузчик |
| [`JpmsAccess`](src/main/java/ru/sprbut/m04/JpmsAccess.java) | 31 | Разница **exports** и **opens**; `InaccessibleObjectException` на закрытом пакете; почему Spring просит `--add-opens` |
| [`GenericTypes`](src/main/java/ru/sprbut/m04/GenericTypes.java) | 32 | Стирание не абсолютно: `ParameterizedType`, `WildcardType`, `TypeVariable`, приём «type token» |
| [`InvocationCost`](src/main/java/ru/sprbut/m04/InvocationCost.java) | 33 | Измеренное сравнение: прямой вызов / `MethodHandle` / кэшированный `Method` / поиск в цикле |
| [`FastAccess`](src/main/java/ru/sprbut/m04/FastAccess.java) | 34 | `MethodHandle`, `privateLookupIn`, `bindTo`; `VarHandle` с `compareAndSet` |
| [`DynamicProxy`](src/main/java/ru/sprbut/m04/DynamicProxy.java) | 35–36 | `Proxy` + `InvocationHandler`, прокси без цели, **self-invocation минует прокси** |
| [`ExecutableApi`](src/main/java/ru/sprbut/m04/ExecutableApi.java) | 36 | `Executable` как общий родитель, `Parameter` с именами, `AnnotatedElement`, `Array.newInstance` |

## Расширенный пример

[`JdkAopFactory`](src/main/java/ru/sprbut/m04/extended/JdkAopFactory.java) — **работающий
мини-AOP на голом JDK**, без Spring и сторонних библиотек. Читает аннотации
[`@Retry`, `@Timed`, `@Cached`, `@Stubbed`](src/main/java/ru/sprbut/m04/extended/Aspects.java)
и применяет их через `InvocationHandler`, вызывая цель кэшированным `MethodHandle`.

```java
PriceService proxy = JdkAopFactory.wrap(PriceService.class, new RealPriceService(), journal);
proxy.compute("ABC");   // cache-miss + timed
proxy.compute("ABC");   // cache-hit — цель не вызвана
```

Тесты фиксируют и ограничение: `proxy.selfCalling(...)` вызывает `compute()` изнутри
объекта, и кэш **не срабатывает**. Это тот же механизм, из-за которого в Spring молча
не работает `@Transactional` на методе, вызванном из соседнего метода того же бина
(модуль 15).

## Ключевые выводы

* JPMS различает **exports** (видно) и **opens** (можно `setAccessible`). `java.base`
  экспортирует почти всё и открывает почти ничего — отсюда флаги `--add-opens`
  в `surefire.argLine` корневого `pom.xml`.
* Стирание типов касается значений, а не объявлений: параметры типа полей, сигнатур
  и суперклассов лежат в атрибуте `Signature` и читаются через `getGenericType()`.
* Дорого не «использовать рефлексию», а **искать член класса на каждом вызове**.
  Ищите один раз, кэшируйте `Method` или, лучше, `MethodHandle` в `static final`.
* Права `MethodHandles.lookup()` определяются местом вызова, а не аргументами:
  свои nestmate'ы видны, чужие приватные члены — только через `privateLookupIn`.
* JDK-прокси реализует **только интерфейсы**. Нет интерфейса — нужен CGLIB-подкласс.
* `equals`/`hashCode`/`toString` в прокси перехватывать нельзя — объект станет неюзабельным.

## Запуск

```bash
mvn -pl 04-reflection-advanced test
```
