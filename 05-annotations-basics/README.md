# Модуль 05 — Аннотации Java

[Слайды 38–45](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p38) ·
[← к списку модулей](../README.md)

## Что в презентации

> `@Annotation` · `@Target{TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE}` ·
> `@Retention{SOURCE, CLASS, RUNTIME}` · `@Inherited` · Для маркировки, без параметров
> (`@Override`) · Single (обычно `value`), указывать имя параметра не обязательно
> `@SuppressWarnings("")` · `@Repeatable` — может быть применена одна и та же аннотация
> много раз к одному участку кода.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`TargetScope`](src/main/java/ru/sprbut/m05/TargetScope.java) | 39 | `@Target` — ограничение компилятора; аннотация локальной переменной не попадает в class-файл вовсе |
| [`RetentionVisibility`](src/main/java/ru/sprbut/m05/RetentionVisibility.java) | 40 | Три политики хранения рядом: из четырёх аннотаций на классе в runtime видна ровно одна |
| [`InheritanceRules`](src/main/java/ru/sprbut/m05/InheritanceRules.java) | 41 | `@Inherited` и три его границы: без него ничего не наследуется, интерфейсы не в счёт, методы тоже |
| [`RepeatableAnnotations`](src/main/java/ru/sprbut/m05/RepeatableAnnotations.java) | 44 | `@Repeatable` и аннотация-контейнер; почему `getAnnotation` возвращает `null` при двух вхождениях |
| [Объявления аннотаций](src/main/java/ru/sprbut/m05/declarations) | 42–44 | Маркерная `@Marker`, single-value `@Level`, `@Inherited @Audited`, повторяемая `@Schedule` + контейнер `@Schedules` |

## Расширенный пример

[`ValidationEngine`](src/main/java/ru/sprbut/m05/extended/ValidationEngine.java) —
работающая мини-версия Bean Validation на собственных
[ограничениях](src/main/java/ru/sprbut/m05/extended/Constraints.java). Каждый вид
аннотации из презентации задействован по назначению:

```java
class User extends BaseEntity {
    @NotBlank @MaxLength(10)                       String login;   // маркерная + single-value
    @Range(min = 18, max = 120)                    int age;        // элементы + defaults
    @Matches(regex = ".+@.+\\..+")
    @Matches(regex = "^[a-z@.]+$")                 String email;   // повторяемая
    @InvisibleNotNull                              String oops;    // retention CLASS — не работает
}

ValidationEngine.validate(user).messages();
```

Отдельный тест фиксирует главную ловушку: поле с `@InvisibleNotNull`
(retention `CLASS`) **молча не проверяется** — в исходнике аннотация есть,
в runtime её нет.

## Ключевые выводы

* Политика хранения по умолчанию — **`CLASS`**, а не `RUNTIME`. Забытый
  `@Retention(RUNTIME)` — самая частая причина «моя аннотация не работает».
* `@Target` проверяет компилятор; в runtime проверять уже нечего.
* `@Inherited` действует **только** по цепочке классов и **только** для аннотаций
  типов. Методы, поля и интерфейсы не наследуют ничего — поэтому Spring обходит
  иерархию сам (модуль 06).
* Повторяемость — сахар: при двух и более вхождениях в байткоде лежит контейнер.
  Читать всегда через `getAnnotationsByType`, никогда — через `getAnnotation`.
* Единственный элемент, названный `value`, можно указывать без имени.

## Запуск

```bash
mvn -pl 05-annotations-basics test
```
