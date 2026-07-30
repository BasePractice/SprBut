# Модуль 06 — Аннотации: детали

[Слайды 47–55](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit#slide=id.p47) ·
[← к списку модулей](../README.md)

## Что в презентации

> `@Documented`, `@Repeatable` · Цели: `ANNOTATION_TYPE`, `PACKAGE`, `TYPE_USE` ·
> Цели: `TYPE_PARAMETER`, `MODULE`, `RECORD_COMPONENT` · Типы элементов: примитив,
> `String`, `Class`, enum, также вложенная аннотация и массив · default-значения
> элементов · Чтение: `getAnnotation`, `isAnnotationPresent` · `@Inherited` работает
> только для классов · **`@RestController` = `@Controller` + `@ResponseBody`**.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`AnnotationMembers`](src/main/java/ru/sprbut/m06/members/AnnotationMembers.java) | 50–52 | Аннотация со всеми шестью допустимыми типами элементов и массивами; чтение `default`-значений отдельно от фактических |
| [`TypeUse`](src/main/java/ru/sprbut/m06/targets/TypeUse.java) | 48–49 | `TYPE_USE` внутри дженерика (`List<@NonNull String>`), `TYPE_PARAMETER`, `RECORD_COMPONENT`, `ANNOTATION_TYPE` |
| [`MetaAnnotated`](src/main/java/ru/sprbut/m06/MetaAnnotated.java) | 55 | Композиция мета-аннотаций и рекурсивный поиск; почему `getAnnotation(Controller.class)` возвращает `null` |

## Расширенный пример

[`MergedAnnotation`](src/main/java/ru/sprbut/m06/extended/MergedAnnotation.java) —
рабочая мини-версия `AnnotatedElementUtils.findMergedAnnotation` из Spring. Делает
то, чего **не делает сам язык**: находит аннотацию через цепочку мета-аннотаций
любой длины и **сливает значения элементов**, уважая
[`@AliasFor`](src/main/java/ru/sprbut/m06/extended/AliasFor.java).

```java
@GetJson("/users/json")            // @GetJson → @GetMapping → @RequestMapping
public void json() {}

new MergedAnnotation<>(method, RequestMapping.class).orElseThrow();
// path = /users/json   (из @GetJson через @AliasFor)
// method = GET         (из @GetMapping, никем не переопределён)
// produces = [application/json]  (из @GetJson → @GetMapping, по совпадению имени)
```

Правила слияния — те же, что в Spring: `@AliasFor` переопределяет явно названный
элемент; одноимённый элемент переопределяет по совпадению имени, но только если
его значение отличается от `default`; ближайшая к элементу аннотация выигрывает
у дальней.

## Ключевые выводы

* `@RestController` ведёт себя как `@Controller` **не потому, что так устроена Java**,
  а потому что так написан читающий код. Язык мета-аннотации не раскрывает.
* Список допустимых типов элементов закрыт: примитив, `String`, `Class`, enum,
  аннотация, массив из них. Причина — значения должны быть константами компиляции.
* `TYPE_USE` — единственный способ аннотировать тип внутри дженерика; читается
  через `AnnotatedType`, а не через `getAnnotations()` поля.
* При рекурсивном обходе мета-аннотаций обязательно фильтруйте `java.lang.annotation.*`:
  `@Retention` помечена `@Retention`, и обход зациклится.
* Элементы аннотации — это методы интерфейса, поэтому их перечисляет
  `getDeclaredMethods()`, а `default`-значения читаются через `getDefaultValue()`.

## Запуск

```bash
mvn -pl 06-annotations-advanced test
```
