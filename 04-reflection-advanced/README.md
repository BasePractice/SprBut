# Модуль 04 — Reflection: детали

[Слайды 31–38, СХЕМА 2 (слайд 39)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p30) ·
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
| [`LoadedClass`](src/main/java/ru/sprbut/m04/LoadedClass.java) | 30 | `forName` и загрузка без инициализации: сканеры classpath берут метаданные без побочных эффектов |
| [`ModuleAccess`](src/main/java/ru/sprbut/m04/ModuleAccess.java) | 31 | Разница **exports** и **opens** — почему Spring просит `--add-opens` |
| [`DeepAccess`](src/main/java/ru/sprbut/m04/DeepAccess.java) | 31 | `InaccessibleObjectException` на закрытом пакете: успех зависит не от модификатора |
| [`GenericType`](src/main/java/ru/sprbut/m04/GenericType.java) | 32 | Стирание не абсолютно: `ParameterizedType`, `WildcardType`, `TypeVariable` |
| [`TypeToken`](src/main/java/ru/sprbut/m04/TypeToken.java) | 32 | Приём «type token» — основа `TypeReference` и `ParameterizedTypeReference` |
| [`InvocationCost`](src/main/java/ru/sprbut/m04/InvocationCost.java) | 33 | Измеренное сравнение: прямой вызов / `MethodHandle` / кэшированный `Method` / поиск в цикле |
| [`Handles`](src/main/java/ru/sprbut/m04/Handles.java) | 34 | `MethodHandle`, `privateLookupIn`; `VarHandle` с `compareAndSet` |
| [`LoggingProxy`](src/main/java/ru/sprbut/m04/LoggingProxy.java) | 35–36 | `Proxy` + `InvocationHandler`, **self-invocation минует прокси** |
| [`StubProxy`](src/main/java/ru/sprbut/m04/StubProxy.java) | 35 | Прокси без цели — так работают репозитории Spring Data |
| [`Parameters`](src/main/java/ru/sprbut/m04/Parameters.java) | 36 | `Executable` как общий родитель, `Parameter` с именами, точки внедрения |
| [`ReflectiveArray`](src/main/java/ru/sprbut/m04/ReflectiveArray.java) | 36 | `Array.newInstance`: `new T[n]` невозможен из-за стирания |

## Расширенный пример

[`Aspected`](src/main/java/ru/sprbut/m04/extended/Aspected.java) — **работающий
мини-AOP на голом JDK**, без Spring и сторонних библиотек. Читает аннотации
[`@Retry`](src/main/java/ru/sprbut/m04/extended/Retry.java),
[`@Timed`](src/main/java/ru/sprbut/m04/extended/Timed.java),
[`@Cached`](src/main/java/ru/sprbut/m04/extended/Cached.java),
[`@Stubbed`](src/main/java/ru/sprbut/m04/extended/Stubbed.java) и применяет их через
[`AspectHandler`](src/main/java/ru/sprbut/m04/extended/AspectHandler.java),
вызывая цель кэшированным `MethodHandle`.

```java
PriceService proxy = new Aspected<>(PriceService.class, new RealPriceService(), journal).proxy();
proxy.price("ABC");   // cache-miss + timed
proxy.price("ABC");   // cache-hit — цель не вызвана
```

Аннотации ищутся на методе **реализации**, а не интерфейса
([`TargetMethod`](src/main/java/ru/sprbut/m04/extended/TargetMethod.java)) — та же
ловушка, что подстерегает в модуле 27 с JDK-прокси и `@Audited`.

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
