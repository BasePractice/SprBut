# Модуль 12 — DI: способы внедрения

[Слайды 99–107, СХЕМА 6 (слайд 108)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p95) ·
[← к списку модулей](../README.md)

## Что в презентации

> IoC — это не DIP из SOLID. Через конструктор, сеттер или поле. Конструктор
> предпочтителен: `final`, обязательность. Внедрение в поле мешает тестам
> без контейнера. Циклические зависимости и `@Lazy`. Service Locator — антипаттерн.
> jakarta: `@Inject`, `@Named`, `@Resource`. Альтернативы: Guice, Dagger, Micronaut, Quarkus.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`ConstructorInjected`](src/main/java/ru/sprbut/m12/injection/ConstructorInjected.java) | 91–92 | Три преимущества конструктора, каждое проверено тестом |
| [`SetterInjected`](src/main/java/ru/sprbut/m12/injection/SetterInjected.java) | 91 | Единственный уместный случай: необязательная зависимость |
| [`FieldInjected`](src/main/java/ru/sprbut/m12/injection/FieldInjected.java) | 93 | Короче всех в записи и хуже всех в тестах |
| [`CircularBeans`](src/main/java/ru/sprbut/m12/cycles/CircularBeans.java) | 94 | Цикл через конструкторы неразрешим в принципе |
| [`ServiceLocatorDemo`](src/main/java/ru/sprbut/m12/locator/ServiceLocatorDemo.java) | 95 | Инверсия не произошла, а только замаскировалась |
| [`JakartaInjected`](src/main/java/ru/sprbut/m12/jakarta/JakartaInjected.java) | 96 | JSR-330 и JSR-250: стандарт, а не изобретение Spring |

## Расширенный пример

[`InjectionAudit`](src/main/java/ru/sprbut/m12/extended/InjectionAudit.java) — аудитор
точек внедрения: разбирает класс рефлексией и выносит вердикт о том, как в нём
организованы зависимости.

Ценность в том, что советы со слайдов перестают быть советами и становятся
**проверяемыми правилами**: поле без `final`, зависимость, полученную из локатора,
или конструктор на восемь аргументов видно машинально, а не на code review.

## Ключевые выводы

* `final` у поля — не украшение: оно физически запрещает объекту существовать
  в недособранном виде.
* Тест, которому для создания объекта нужен контейнер, — признак внедрения в поле.
* Service Locator внешне неотличим от DI: разница в том, кто делает шаг —
  объект идёт за зависимостью сам или получает её.
* `@Lazy` разрывает цикл, но не убирает его. Настоящее лечение — разделить бины.

## Запуск

```bash
mvn -pl 12-di-injection test
```
