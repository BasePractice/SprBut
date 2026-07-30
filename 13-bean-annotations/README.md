# Модуль 13 — Bean: аннотации контейнера

[Слайды 101–108](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p101) ·
[← к списку модулей](../README.md)

## Что в презентации

> `@Bean`. `@Scope` (singleton, prototype, request, session). `@Autowired`.
> `@Qualifier`. `@Primary`, `@Lazy`, `@DependsOn`. `@Conditional`, `@Profile`.
> `@PostConstruct`, `@PreDestroy`. `@Component` vs `@Bean`: свой класс или чужой.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`ScopeConfig`](src/main/java/ru/sprbut/m13/scopes/ScopeConfig.java) | 101 | Сколько экземпляров существует и как долго они живут |
| [`QualifierConfig`](src/main/java/ru/sprbut/m13/qualifiers/QualifierConfig.java) | 102–103 | Порядок разрешения: тип → `@Primary` → `@Qualifier` → имя |
| [`ConditionalConfig`](src/main/java/ru/sprbut/m13/conditional/ConditionalConfig.java) | 104–106 | `@Primary`, `@Lazy`, `@DependsOn`, `@Conditional`, `@Profile` |
| [`ComponentVsBean`](src/main/java/ru/sprbut/m13/componentvsbean/ComponentVsBean.java) | 108 | Единственный практический критерий выбора: чей класс |

## Расширенный пример

[`BeanRegistryReport`](src/main/java/ru/sprbut/m13/extended/BeanRegistryReport.java) —
отчёт о содержимом контейнера: что в нём есть, с каким скоупом, что помечено
`@Primary`, что ленивое, что уже создано, а что ещё нет.

Отдельного внимания стоит последняя колонка. Разница между «бин объявлен»
и «бин создан» на слайдах не видна вовсе, а в жизни именно она объясняет,
почему ошибка конфигурации всплывает не на старте, а при первом обращении.

## Ключевые выводы

* `@Scope` описывает не бин, а **договор о времени жизни** между бином и контейнером.
* `@Primary` действует на стороне объявления, `@Qualifier` — на стороне потребителя.
  Это разные инструменты для разных ситуаций, а не два способа сделать одно.
* `@Lazy` меняет момент создания, а не сам факт: определение бина есть всегда.
* `@Component` — для своих классов, `@Bean` — для чужих. Всё остальное — вкусовщина.

## Запуск

```bash
mvn -pl 13-bean-annotations test
```
