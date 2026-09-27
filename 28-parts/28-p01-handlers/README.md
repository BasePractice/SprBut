# Часть 28 p01 — участники обработки запроса

[← к списку модулей](../../README.md) · [модуль 20 «Spring MVC»](../../20-spring-mvc/README.md)

## О чём эта часть

Модуль 20 отвечает на вопрос, что происходит между сокетом и вызовом метода
контроллера. Эта часть — продолжение того же разговора с другой стороны:
в разрыв между сокетом и методом можно встать самому, и мест для этого пять.
Каждое со своей областью видимости, и выбор места — это выбор того, что
участнику будет видно.

| Участник | Где стоит | Что видит | Чего уже (или ещё) не видит |
|---|---|---|---|
| `Filter` | до `DispatcherServlet` | сырые запрос и ответ | контроллер, маршрут, аргументы |
| `HandlerInterceptor` | внутри диспетчера | выбранный обработчик | тело запроса — оно уже прочитано |
| `Converter` | при сборке аргумента | одно значение и два типа | запрос целиком |
| `HandlerMethodArgumentResolver` | при сборке аргумента | запрос и описание параметра | другие аргументы метода |
| `RequestBodyAdvice` | между конвертером сообщений и методом | разобранное тело | результат проверки — её ещё не было |

## Примеры

| Класс | Что показывает |
|---|---|
| [`SanitizingFilter`](src/main/java/ru/sprbut/m28/filters/SanitizingFilter.java) | Тело читается один раз, поэтому изменить его можно только обёрткой |
| [`SanitizedRequest`](src/main/java/ru/sprbut/m28/filters/SanitizedRequest.java) | `HttpServletRequestWrapper`: делегирует всё, переопределяет два метода |
| [`TimingInterceptor`](src/main/java/ru/sprbut/m28/interceptors/TimingInterceptor.java) | `preHandle` знает обработчик и умеет прервать обработку |
| [`LowerCaseConverter`](src/main/java/ru/sprbut/m28/converters/LowerCaseConverter.java) | Конвертер подбирается по паре типов — и в этом всё дело |
| [`CurrentUser`](src/main/java/ru/sprbut/m28/handlers/CurrentUser.java) | Аннотация-метка, за которой стоит не магия, а резолвер |
| [`CurrentUserArgumentResolver`](src/main/java/ru/sprbut/m28/handlers/CurrentUserArgumentResolver.java) | Свой источник аргумента: заголовок вместо пути и параметра |
| [`MaskingRequestBodyAdvice`](src/main/java/ru/sprbut/m28/advices/MaskingRequestBodyAdvice.java) | Правка тела между разбором JSON и вызовом метода |
| [`Failures`](src/main/java/ru/sprbut/m28/web/Failures.java) | Нарушенные правила проверки, собранные в `ProblemDetail` |
| [`HandlersConfig`](src/main/java/ru/sprbut/m28/config/HandlersConfig.java) | Регистрация тех, кого контейнер не находит сам |
| [`UserController`](src/main/java/ru/sprbut/m28/web/UserController.java) | Контроллер в три строки: интересное случилось до него |

## Ключевые выводы

* **`@Component` регистрирует не всех.** Фильтр подхватит контейнер сервлетов,
  конвертер — автоконфигурация MVC, а интерсептор и резолвер аргумента так и
  останутся бинами, о которых диспетчер не знает: их списки он берёт из
  `WebMvcConfigurer`. Отсюда `HandlersConfig`.
* **`Converter<String, String>` не вызовется никогда.** `ConversionService`
  подбирает конвертер по паре «откуда, куда» и не преобразует тип в него же.
  Приведение к нижнему регистру начинает работать ровно тогда, когда
  у параметра появляется собственный тип — [`Login`](src/main/java/ru/sprbut/m28/dto/Login.java).
* **Тело запроса читается один раз.** Фильтр, прочитавший поток и не
  подменивший запрос обёрткой, оставляет контроллеру пустое тело. Читать
  его стоит не у всех запросов подряд: у формы тело разбирает контейнер,
  и перехваченный поток ломает `getParameter`.
* **Совет над телом срабатывает до проверки.** `@Valid` смотрит уже на то,
  что вернул `afterBodyRead`, поэтому маскировать почту безопасно, а
  рассчитывать в совете на её правильность — нет.
* **`final` и `@Configuration` не дружат по умолчанию.** Контейнер расширяет
  класс конфигурации через CGLIB ради перехвата `@Bean`-методов; расширить
  `final` класс нельзя, и контекст падает на старте. Лечится не снятием
  `final`, а `proxyBeanMethods = false` там, где `@Bean`-методов нет.

## Запуск

```bash
mvn -pl 28-parts/28-p01-handlers test
```

Проверить участников на живом приложении:

```bash
mvn -pl 28-parts/28-p01-handlers spring-boot:run
curl -i 'localhost:8084/api/users/greet?name=IVAN'
curl -i -H 'X-User: IVAN' localhost:8084/api/users/me
curl -i -H 'Content-Type: application/json' \
  -d '{"username":"<script>x</script>ivan","email":"ivan@example.com"}' \
  localhost:8084/api/users
```
