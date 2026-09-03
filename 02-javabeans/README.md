# Модуль 02 — JavaBeans

[Слайды 12–19, СХЕМА 17 (слайд 20)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p12) ·
[← к списку модулей](../README.md)

## Что в презентации

> Соглашение. Класс, который подчиняется соглашению, называется Bean. Должен иметь
> публичный конструктор без параметров. Свойства объекта должны быть доступны через
> setter и getter. Должен реализовать `Serializable` (Spring не требует). Используется
> в таких фреймворках как Spring, Hibernate, JavaEE. **Избыточность и мутабельность.**
> Lombok, record, Immutability (Builder).

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`CustomerBean`](src/main/java/ru/sprbut/m02/classic/CustomerBean.java) | 12–17 | Классический бин по всем правилам — и 60 строк шаблона на 5 свойств |
| [`BeanVerdict`](src/main/java/ru/sprbut/m02/classic/BeanVerdict.java) | 12–16 | Проверка соглашения рефлексией: строгая и «как у Spring» (без `Serializable`) |
| [`BeanProperties`](src/main/java/ru/sprbut/m02/classic/BeanProperties.java) | 14 | Свойство определяется методами, а не полями |
| [`Introspected`](src/main/java/ru/sprbut/m02/classic/Introspected.java) | 16 | Штатный `java.beans.Introspector` и `PropertyDescriptor` |
| [`BeanValue`](src/main/java/ru/sprbut/m02/classic/BeanValue.java) | 16 | Чтение и запись свойства по строковому имени |
| [`EmptyBean`](src/main/java/ru/sprbut/m02/classic/EmptyBean.java) | 18 | Мутабельность: бин создаётся заведомо невалидным |
| [`PropertyKey`](src/main/java/ru/sprbut/m02/classic/PropertyKey.java) | — | Правило `decapitalize`: `URL` остаётся `URL`, `Name` становится `name` |
| [`CustomerRecord`](src/main/java/ru/sprbut/m02/modern/CustomerRecord.java) | 19 | `record`: неизменяемость и валидация в компактном конструкторе |
| [`ImmutableCustomer`](src/main/java/ru/sprbut/m02/modern/ImmutableCustomer.java) | 19 | Builder + защитное копирование коллекций |

## Расширенный пример

[`BoundBean`](src/main/java/ru/sprbut/m02/extended/BoundBean.java) — мини-биндер
конфигурации: заполняет [`ServerProperties`](src/main/java/ru/sprbut/m02/extended/ServerProperties.java)
из плоской карты «ключ → строка» с конвертацией типов (`int`, `long`, `boolean`, `enum`,
`BigDecimal`, `LocalDate`), поддержкой `kebab-case` и отчётом о непривязанных ключах.

Это работающая модель `@ConfigurationProperties` (модуль 16). Именно ради такого
сценария соглашение и существует: контейнеру нужен конструктор без параметров, чтобы
создать объект, и сеттеры, чтобы его наполнить.

```java
new BoundBean<>(ServerProperties.class, Map.of("ssl-enabled", "true", "port", "8443")).result();
```

Границу применимости соглашения видно тут же: на `record` этот биндер не работает
вовсе — нет ни конструктора без параметров, ни сеттеров. Поэтому для неизменяемых
конфигураций Spring Boot пришлось учить отдельному режиму constructor binding.

## Ключевые выводы

* Свойство определяется **методами, а не полями**: `getFullName()` без поля — это
  полноценное read-only свойство, которое видит `Introspector`.
* «Избыточность и мутабельность» — не абстрактная претензия: бин обязан создаваться
  пустым, то есть заведомо невалидным, и валидировать его можно только постфактум.
* `record` решает обе проблемы, но **перестаёт быть JavaBean**: нет ни конструктора
  без параметров, ни `getXxx`. `Introspector` не видит у record ни одного свойства.
  Поэтому Spring Boot пришлось отдельно учить constructor binding.
* Builder возвращает удобство сборки, не возвращая мутабельность результату.

## Запуск

```bash
mvn -pl 02-javabeans test
```
