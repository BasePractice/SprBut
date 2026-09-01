# Модуль 20 — Тестирование

[Слайды 180–186](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p180) ·
[← к списку модулей](../README.md)

## Что в презентации

> `@SpringBootTest` — полный контекст. Срезы: `@WebMvcTest`, `@DataJpaTest`, `@JsonTest`.
> Контекст кэшируется между тестами. `@MockBean` и `@TestConfiguration`.
> Testcontainers для реальной БД.

На слайдах — Spring Boot 3. Практикум собран на Boot 4.1, где две вещи изменились:
`@MockBean` заменён на `@MockitoBean` из `org.springframework.test.context.bean.override.mockito`,
а срезы разъехались по отдельным стартерам — `@DataJpaTest` приезжает
со `spring-boot-starter-data-jpa-test`, `@WebMvcTest` — со `spring-boot-starter-webmvc-test`,
`@JsonTest` — со `spring-boot-starter-jackson-test`. Сама идея среза не изменилась.

## Примеры

| Класс | Что показывает |
|---|---|
| [`TestingApp`](src/main/java/ru/sprbut/m20/TestingApp.java) | Небольшое приложение, на котором показаны все виды тестов |
| [`ProductController`](src/main/java/ru/sprbut/m20/web/ProductController.java) | Веб-слой для среза `@WebMvcTest` |
| [`ProductRepository`](src/main/java/ru/sprbut/m20/domain/ProductRepository.java) | Интерфейс есть, реализации нет: JDK-прокси плюс разбор имён методов |
| [`CatalogService`](src/main/java/ru/sprbut/m20/domain/CatalogService.java) | В тесте контроллера подменяется, в своём — работает по-настоящему |
| [`SliceTests`](src/test/java/ru/sprbut/m20/SliceTests.java) | Полный контекст и три среза рядом, для сравнения |

## Расширенный пример

Расширенный пример этого модуля — сами [`SliceTests`](src/test/java/ru/sprbut/m20/SliceTests.java):
один и тот же функционал проверен четырьмя способами, от полного контекста
до чистого юнит-теста, и рядом видно, **что каждый способ ловит, а что пропускает**.

Срез — это не «быстрый `@SpringBootTest`». Это другой набор бинов, и потому другой
набор ошибок, которые тест в принципе способен обнаружить.

## Ключевые выводы

* Контекст кэшируется по **конфигурации** теста. Каждая уникальная комбинация
  аннотаций и свойств — новый контекст и новые секунды к сборке.
* `@MockitoBean` меняет состав контекста, а значит порождает отдельный кэш-ключ.
  Именно поэтому им легко замедлить сборку до неприличия.
* `@DataJpaTest` не увидит ошибку в контроллере, `@WebMvcTest` — ошибку в запросе
  к базе. Это не недостаток, а способ локализовать причину.
* Тест, который можно написать без контейнера, стоит написать без контейнера.

## Запуск

```bash
mvn -pl 20-testing test
```
