# Модуль 09 — Reflection vs APT vs байткод

[Слайды 80–85, СХЕМА 4 (слайд 87)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p76) ·
[← к списку модулей](../README.md)

## Что в презентации

> Reflection: runtime, гибко, медленно · APT: compile-time, только генерация, быстро ·
> Байткод (CGLIB, ByteBuddy): и то, и другое · **Spring использует все три механизма** ·
> В native image рефлексия почти недоступна.
>
> СХЕМА 4 (слайд 78) — ось времени: compile-time (APT) против runtime (рефлексия).

## Примеры

Один и тот же маппинг `UserEntity → UserDto`, реализованный тремя способами
за общим интерфейсом [`UserMapper`](src/main/java/ru/sprbut/m09/UserMapper.java):

| Класс | Слайд | Механизм |
|---|---|---|
| [`ReflectiveMapper`](src/main/java/ru/sprbut/m09/ReflectiveMapper.java) | 73 | Правила выводятся в runtime из метаданных — маппер сам находит 5 свойств и сам пропускает `internalNote`, которого нет у цели |
| [`GeneratedStyleMapper`](src/main/java/ru/sprbut/m09/GeneratedStyleMapper.java) | 74 | Прямые вызовы, как в коде, сгенерированном APT |
| [`BytecodeMapper`](src/main/java/ru/sprbut/m09/BytecodeMapper.java) | 75 | ByteBuddy собирает класс **в runtime**; тут же — CGLIB-приём: проксирование класса **без интерфейса** |

## Расширенный пример

[`Mappers`](src/main/java/ru/sprbut/m09/extended/Mappers.java) ставит три реализации
рядом и превращает СХЕМУ 4 в проверяемые утверждения:

```java
new Mappers().agree(entity);                       // true — механизм не влияет на поведение
new Benchmark(new Mappers(), entity).timings(50_000);   // влияет на цену вызова
new RequiredHints().byMapper();
// ReflectiveMapper     → 10 hints: UserEntity#getFirstName, UserDto#setFirstName, …
// GeneratedStyleMapper → []
// BytecodeMapper       → «класс генерируется в runtime — native image неприменим»
```

Замер вынесен в [`Benchmark`](src/main/java/ru/sprbut/m09/extended/Benchmark.java),
а подсчёт подсказок для native image — в
[`RequiredHints`](src/main/java/ru/sprbut/m09/extended/RequiredHints.java).

[`MechanismProfile`](src/main/java/ru/sprbut/m09/extended/MechanismProfile.java) оформляет
свойства механизмов как данные: фаза, гибкость, скорость, типобезопасность,
пригодность для native image и **конкретный список того, для чего Spring использует
каждый из трёх**.

## Ключевые выводы

* Выбор механизма **не меняет поведение** — меняет свойства: гибкость, скорость,
  момент обнаружения ошибок, пригодность для native image.
* Гибкость и типобезопасность здесь взаимоисключающи. Рефлексия подхватит новое
  поле без пересборки, но опечатку покажет только в runtime. APT — наоборот.
* Байткод берёт гибкость **и** скорость, платя типобезопасностью и совместимостью
  с native image: класса ещё не существует на этапе сборки.
* Spring использует все три, и у каждого своя зона: рефлексия — `@Autowired` и `@Value`,
  байткод — CGLIB-прокси для `@Transactional`, APT — `spring-boot-configuration-processor`
  и Spring AOT.
* В native image выживает только compile-time механизм — это и есть причина, по которой
  Spring 6 обзавёлся AOT (модуль 26).

## Запуск

```bash
mvn -pl 09-reflection-vs-apt test
```
