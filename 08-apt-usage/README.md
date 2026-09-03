# Модуль 08 — APT: сборка

[Слайды 72–77, СХЕМА 20 (слайд 78)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.p69) ·
[← к списку модулей](../README.md)

## Что в презентации

> Регистрация: `META-INF/services` или `@AutoService` · Maven/Gradle:
> `annotationProcessor`, `-processorpath` · Раунды обработки, `RoundEnvironment` ·
> `Filer` для записи, **JavaPoet** для генерации · `@SupportedAnnotationTypes`,
> `@SupportedSourceVersion`.

Модуль 07 писал процессоры. Здесь они **подключены к настоящей сборке Maven**,
и сгенерированный код используется как обычный.

## Примеры

| Файл | Слайд | Что показывает |
|---|---|---|
| [`pom.xml`](pom.xml) | 67, 70 | `annotationProcessorPaths` — процессоры на отдельном пути, не в classpath проекта; опции `-Aregistry.package=...` |
| [`ProcessorRegistration`](src/main/java/ru/sprbut/m08/ProcessorRegistration.java) | 66 | Как javac находит процессоры: `ServiceLoader` читает `META-INF/services`. `@AutoService` — просто генератор этого файла |
| [`Customer`](src/main/java/ru/sprbut/m08/model/Customer.java), [`Order`](src/main/java/ru/sprbut/m08/model/Order.java) | 58 | Бины с `@GenerateBuilder`; у `Order` переопределён суффикс — получится `OrderMaker` |
| [`CustomerRepository`](src/main/java/ru/sprbut/m08/service/CustomerRepository.java) и др. | — | Классы с `@Registered`, попадающие в сгенерированный реестр |

## Расширенный пример

[`CheckoutFacade`](src/main/java/ru/sprbut/m08/extended/CheckoutFacade.java) — рабочий код,
который импортирует **три класса, которых нет ни в одном файле `src`**:

```java
import ru.sprbut.m08.generated.ModuleRegistry;   // JavaPoet, из @Registered
import ru.sprbut.m08.model.CustomerBuilder;      // из @GenerateBuilder
import ru.sprbut.m08.model.OrderMaker;           // тот же процессор, другой суффикс

Customer c = CustomerBuilder.create().id("C-1").name("Иванов").vip(true).build();
var repo   = (CustomerRepository) ModuleRegistry.create("customers");
```

Ни одной строчки рефлексии: зависимости достаются из реестра, который знает
конструкторы статически. Тест на этот класс сам по себе — доказательство работы APT:
если бы процессор не отработал, он бы **не скомпилировался**.

## Как это собирается

```
src/main/java/**.java
        │
        ▼  javac, раунд 1
   ┌──────────────────┐   annotationProcessorPaths
   │ BuilderProcessor │◄── 07-annotation-processor.jar
   │ RegistryProcessor│    javapoet.jar
   │ TodoProcessor    │
   └──────────────────┘
        │ Filer
        ▼
target/generated-sources/annotations/
    ru/sprbut/m08/model/CustomerBuilder.java
    ru/sprbut/m08/model/OrderMaker.java
    ru/sprbut/m08/generated/ModuleRegistry.java
        │
        ▼  javac, раунд 2 — компилируется вместе с обычным кодом
target/classes/
```

При сборке в логе видны предупреждения от `TodoProcessor` — это тот же конвейер,
только ветка «анализ» вместо «генерация».

## Ключевые выводы

* Процессоры живут на **отдельном пути** (`-processorpath` / `annotationProcessorPaths`),
  а не в classpath проекта — их код не попадает в итоговый артефакт.
* Зависимость на модуль с аннотациями — `provided`: у аннотаций retention `SOURCE`,
  в байткод они не попадают, в runtime не нужны.
* `annotationProcessorPaths` резолвится **из локального репозитория, а не из реактора**.
  Поэтому корневая команда сборки — `mvn clean install`, а не `mvn test`:
  модуль 07 должен успеть установиться до компиляции модуля 08.
* Сгенерированный код — обычный код: IDE его видит, типы проверяются, переход
  к определению работает. Разница только в том, где лежит исходник.

## Запуск

```bash
mvn -pl 07-annotation-processor install   # процессор должен попасть в ~/.m2
mvn -pl 08-apt-usage test
```
