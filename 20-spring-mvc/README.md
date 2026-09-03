# Модуль 20 — Spring MVC

[Слайды 193–201](https://docs.google.com/presentation/d/1bsebBSBpseNGlDBeEnZbYGq1Kt5A6qHR/edit?usp=sharing&ouid=103425146937158568285&rtpof=true&sd=true) ·
[← к списку модулей](../README.md)

## Что в презентации

> `DispatcherServlet` — единая точка входа для всех запросов. `@Controller`
> отдаёт представление, `@RestController` — тело. `@RequestMapping` и его
> сокращения: `@GetMapping`, `@PostMapping`. Аргументы метода:
> `@PathVariable`, `@RequestParam`, `@RequestBody`. `ResponseEntity`: тело,
> код состояния и заголовки вместе. `@ControllerAdvice` собирает обработку
> ошибок в одном месте. Один запрос — один поток, поток занят до конца
> обработки.

Модуль отвечает на один вопрос: что происходит между сокетом и вызовом
вашего метода.

## Примеры

| Класс | Слайд | Что показывает |
|---|---|---|
| [`MvcApp`](src/main/java/ru/sprbut/m20/MvcApp.java) | 194 | Одна аннотация — и в контексте есть `DispatcherServlet` |
| [`NoteController`](src/main/java/ru/sprbut/m20/web/NoteController.java) | 195–198 | Аннотации маршрута, три способа получить данные запроса, полный ответ |
| [`PageController`](src/main/java/ru/sprbut/m20/web/PageController.java) | 195 | Та же строка возврата, но это имя представления, а не тело |
| [`NewNoteRequest`](src/main/java/ru/sprbut/m20/web/NewNoteRequest.java) | 197 | Тело запроса как `record` с правилами проверки |
| [`Failures`](src/main/java/ru/sprbut/m20/web/Failures.java) | 199 | `@RestControllerAdvice` и `ProblemDetail` из RFC 9457 |
| [`Notes`](src/main/java/ru/sprbut/m20/domain/Notes.java) | — | Хранилище в памяти: за контроллером не должно быть ничего интересного |

## Расширенный пример

[`RouteMap`](src/main/java/ru/sprbut/m20/extended/RouteMap.java) — таблица
маршрутов, снятая с самого приложения.

Ровно её `DispatcherServlet` просматривает на каждый запрос, выбирая метод для
вызова. Аннотации `@GetMapping` и `@PostMapping` к этому моменту давно
прочитаны, а результат чтения лежит в `RequestMappingHandlerMapping` — контейнер
отдаёт его как обычный бин.

Здесь курс сходится сам с собой: аннотация — это метаданные (модули 05–06),
кто-то должен их прочесть (модуль 01), контейнер отдаёт готовый результат
чтения (модули 11–14). Практический смысл тот же, что у `/actuator/mappings`:
когда запрос уходит «не туда», ответ виден в этой таблице, а не в исходниках.

## Ключевые выводы

* `DispatcherServlet` — обычный сервлет, зарегистрированный на «/»
  автоконфигурацией. Всё остальное в MVC — то, что он делает между приёмом
  запроса и вызовом метода.
* Разница между `@Controller` и `@RestController` — в смысле возвращаемого
  значения: имя представления против тела ответа. Аннотация одна, следствие
  разное.
* Контроллер не читает `HttpServletRequest`: значения аргументов уже разобраны
  за него. Это и есть работа, которую делает диспетчер.
* `@Valid` не проверяет сам — он включает проверку. Ответ об ошибке рождается
  в `@ControllerAdvice`, а не в контроллере.
* Поток занят от начала запроса до конца ответа. Пока это дёшево — сервлетная
  модель проще; когда становится дорого, начинается [модуль 21](../21-webflux/README.md).

## Запуск

```bash
mvn -pl 20-spring-mvc test
```
