# Модуль 15 — Модули Spring и Spring AOP

[Слайды 132–139, СХЕМА 8 (слайд 133) и СХЕМА 9 (слайд 140)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p127) ·
[← к списку модулей](../README.md)

## Что в презентации

> Spring Core, Boot, Data, Security, Cloud, MVC, AOP, JDBC, Transaction.
>
> Прокси вокруг бина, класс не меняется. JDK dynamic proxy — если есть интерфейс.
> CGLIB-подкласс — если интерфейса нет. Self-invocation не перехватывается.
> `@Transactional` и `@Cacheable` работают так же.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`SpringModuleMap`](src/main/java/ru/sprbut/m15/modules/SpringModuleMap.java) | 120 | Карта модулей: что на чём стоит |
| [`AuditAspect`](src/main/java/ru/sprbut/m15/aop/AuditAspect.java) | 121–126 | Аспект как пара «что сделать» и «где»; целевой класс не меняется |
| [`DiscountService`](src/main/java/ru/sprbut/m15/aop/DiscountService.java) | 122 | Есть интерфейс — JDK-прокси, и бин уже не получить по классу реализации |
| [`PricingService`](src/main/java/ru/sprbut/m15/aop/PricingService.java) | 123–124 | Нет интерфейса — CGLIB-подкласс; здесь же self-invocation |
| [`AopConfig`](src/main/java/ru/sprbut/m15/aop/AopConfig.java) | — | `@EnableAspectJAutoProxy` — это всего лишь ещё один `BeanPostProcessor` |

## Расширенный пример

[`Retryable`](src/main/java/ru/sprbut/m15/extended/Retryable.java) +
[`RetryAspect`](src/main/java/ru/sprbut/m15/extended/RetryAspect.java) — собственная
аннотация повторов, работающая по той же схеме, что `@Transactional`.

Главная часть — [`PaymentService`](src/main/java/ru/sprbut/m15/extended/PaymentService.java):
один и тот же вызов, сделанный **четырьмя способами**. Через прокси, через `this`
(аспект молчит), через `AopContext.currentProxy()` и через
[`ChargeExecutor`](src/main/java/ru/sprbut/m15/extended/ChargeExecutor.java) — отдельный
бин. Ограничение self-invocation после этого перестаёт быть абстракцией.

## Ключевые выводы

* Прокси — это **другой объект**. Всё поведение AOP следует из одного этого факта.
* Вызов через `this` идёт мимо обёртки. Ни `@Transactional`, ни `@Cacheable`,
  ни свой аспект в этом случае не сработают — и ошибки не будет.
* `exposeProxy = true` позволяет достать себя-в-прокси, но это лечение симптома:
  честный выход — вынести метод в соседний бин.
* Аннотация не «включает» поведение. Его включает `BeanPostProcessor`,
  который эту аннотацию ищет.

## Запуск

```bash
mvn -pl 15-spring-modules-aop test
```
