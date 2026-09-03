# Модуль 25 — Типичные ошибки контейнера

[Слайды 258–264, СХЕМА 23 (слайд 265)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p201) ·
[← к списку модулей](../README.md)

## Что в презентации

> `NoSuchBeanDefinitionException`: бин не найден. `NoUniqueBeanDefinition`: `@Qualifier`
> или `@Primary`. Circular reference: разделить бины или `@Lazy`.
> `BeanCurrentlyInCreationException`. Prototype в singleton: нужен `proxyMode`.
> Диагностика: `--debug`, отчёт об условиях.

Каждая ошибка показана дважды: в виде падающей конфигурации и в виде вылеченной.

## Примеры

| Класс | Что показывает |
|---|---|
| [`MissingBeanConfig`](src/main/java/ru/sprbut/m25/missing/MissingBeanConfig.java) | `NoSuchBeanDefinitionException`: зависимости нет ни в одном экземпляре |
| [`RepairedBeanConfig`](src/main/java/ru/sprbut/m25/missing/RepairedBeanConfig.java) | Лечение: один `@Bean`-метод поднимает весь контекст |
| [`AmbiguousConfig`](src/main/java/ru/sprbut/m25/ambiguous/AmbiguousConfig.java) | `NoUniqueBeanDefinitionException`: два кандидата на одну точку внедрения |
| [`PrimaryConfig`](src/main/java/ru/sprbut/m25/ambiguous/PrimaryConfig.java) | Лечение `@Primary` — выбор на стороне объявления бина |
| [`QualifierConfig`](src/main/java/ru/sprbut/m25/ambiguous/QualifierConfig.java) | Лечение `@Qualifier` — выбор на стороне потребителя |
| [`CircularConfig`](src/main/java/ru/sprbut/m25/circular/CircularConfig.java) | `BeanCurrentlyInCreationException`: взаимные конструкторы |
| [`LazyConfig`](src/main/java/ru/sprbut/m25/circular/LazyConfig.java) | Обезболивающее `@Lazy`: цикл остался, но виден перестал быть |
| [`SplitConfig`](src/main/java/ru/sprbut/m25/circular/SplitConfig.java) | Честное лечение: разделить бины, цикла не станет |
| [`PlainScopeConfig`](src/main/java/ru/sprbut/m25/scopes/PlainScopeConfig.java) | Prototype в singleton: контекст цел, поведение неверно |
| [`ProxiedScopeConfig`](src/main/java/ru/sprbut/m25/scopes/ProxiedScopeConfig.java) | Лечение `proxyMode = TARGET_CLASS` |

## Расширенный пример

[`Health`](src/main/java/ru/sprbut/m25/extended/Health.java) — диагност конфигурации.
Поднимает контекст в изоляции и, если тот падает, превращает стектрейс на двести
строк в две фразы: что сломалось и что делать.

```java
Diagnosis diagnosis = new Health(AmbiguousConfig.class).diagnosis();

diagnosis.summary();  // на одну точку внедрения нашлось несколько бинов: express, economy
diagnosis.remedy();   // пометить обычную реализацию @Primary либо назвать нужную ...
```

Это учебная модель `FailureAnalyzer` из Spring Boot — того самого механизма, который
печатает блок `***************************\nAPPLICATION FAILED TO START` вместо
стектрейса. Внутри — цепочка [`Diagnosis`](src/main/java/ru/sprbut/m25/Diagnosis.java):
[`MissingBean`](src/main/java/ru/sprbut/m25/extended/MissingBean.java),
[`AmbiguousBean`](src/main/java/ru/sprbut/m25/extended/AmbiguousBean.java),
[`CircularReference`](src/main/java/ru/sprbut/m25/extended/CircularReference.java),
[`UnknownFailure`](src/main/java/ru/sprbut/m25/extended/UnknownFailure.java).

## Ключевые выводы

* Настоящая причина никогда не лежит в вершине стека: сверху `UnsatisfiedDependencyException`,
  интересное — этажами ниже, в цепочке `getCause()`.
* `NoUniqueBeanDefinitionException` **наследует** `NoSuchBeanDefinitionException`.
  Перепутанный порядок `instanceof` превращает «бинов слишком много» в «бина нет».
* Внедрение через конструктор ловит цикл на старте; через поле — прячет его,
  подсунув недоинициализированную ссылку. Падение на старте здесь достоинство.
* Ошибка с `prototype` внутри `singleton` **не роняет контекст**. Это худший вид
  ошибки: аннотация есть, а смысла в ней нет, и тест на живой контекст ничего не заметит.
* `@Lazy` разрывает цикл отсрочкой, а не лечит его. Взаимная зависимость почти всегда
  означает, что два класса делят одну ответственность.

## Запуск

```bash
mvn -pl 25-common-mistakes test
```
