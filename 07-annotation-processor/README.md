# Модуль 07 — Annotation processor

[Слайды 63–69, СХЕМА 3 (слайд 70)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p60) ·
[← к списку модулей](../README.md)

## Что в презентации

> Генерация исходного кода · Изменение исходного кода (хак AST, как Lombok) ·
> Анализ исходного кода · Основное применение — уменьшение количества кода,
> которое пишут руками · `AbstractProcessor`.
>
> СХЕМА 3 (слайд 64) — конвейер APT: исходники → раунды javac → процессор → новый код.

В этом модуле лежат **сами процессоры**. Модуль 08 подключает их к реальной сборке.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`BuilderProcessor`](src/main/java/ru/sprbut/m07/BuilderProcessor.java) | 58, 60 | Генерация исходника через `Filer` + анализ кода через `Messager`: класс обязан быть JavaBean, иначе сборка падает |
| [`TodoProcessor`](src/main/java/ru/sprbut/m07/TodoProcessor.java) | 60 | Процессор, который **ничего не генерирует** — только диагностика, как Error Prone и NullAway |
| [Аннотации](src/main/java/ru/sprbut/m07/api) | 58–60 | `@GenerateBuilder`, `@Registered`, `@Todo` — все с `RetentionPolicy.SOURCE` |
| [`META-INF/services`](src/main/resources/META-INF/services/javax.annotation.processing.Processor) | 66 | Регистрация вручную, без `@AutoService` — чтобы механизм был виден целиком |

## Расширенный пример

[`RegistryProcessor`](src/main/java/ru/sprbut/m07/extended/RegistryProcessor.java) — собирает
все классы с `@Registered` в **один сгенерированный реестр**. Это compile-time аналог
`@ComponentScan`: список компонентов известен уже при сборке.

```java
// сгенерировано JavaPoet'ом
private static final Map<String, Supplier<Object>> FACTORIES =
        Map.of("customers", CustomerRepository::new, "audit", AuditLog::new);
```

Здесь есть всё, чего нет в простом процессоре: накопление данных между раундами,
**JavaPoet** вместо склейки строк, опции процессора (`-Aregistry.package=...`),
и главное — ссылки на конструкторы `Xxx::new` вместо `Class.forName`. Именно
поэтому такой код работает в native image (модуль 22).

## Как тестируется процессор

Процессор нельзя проверить обычным unit-тестом: он живёт внутри `javac` и работает
не с объектами, а с моделью исходного текста. Поэтому
[`CompilationHarness`](src/test/java/ru/sprbut/m07/CompilationHarness.java)
**по-настоящему запускает компилятор** через `javax.tools`, а тесты проверяют
и сгенерированный текст, и диагностику, и поведение загруженного класса.

## Ключевые выводы

* Процессор работает с `javax.lang.model.element.Element` — моделью **исходного
  текста**. Никаких `Class`: классов ещё не существует, загружать нечего.
* Штатное API умеет только **создавать** новые файлы, но не изменять существующие.
  Lombok добивается изменения хаком внутреннего AST компилятора.
* Файл надо создавать **через `Filer`**, а не `new FileWriter` — иначе javac
  не узнает о новом коде и не скомпилирует его.
* Файл, созданный в **последнем** раунде (`processingOver()`), компилируется,
  но его нельзя импортировать из обычного кода — javac предупреждает
  «created in the last round». Генерировать надо в рабочем раунде.
* Процессор вызывается, **только если** в раунде есть аннотации из
  `@SupportedAnnotationTypes`. Чтобы вызываться всегда, надо объявить `"*"`.
* `return true` из `process()` означает «аннотацию поглотил, другим не передавать».

## Запуск

```bash
mvn -pl 07-annotation-processor test
```
