# Часть 28 p01 — участники обработки запроса

[← к списку модулей](../../README.md) · [модуль 20 «Spring MVC»](../../20-spring-mvc/README.md)

## О чём эта часть

Модуль 20 отвечает на вопрос, что происходит между сокетом и вызовом метода
контроллера. Эта часть — продолжение того же разговора с другой стороны:
в разрыв между сокетом и методом, а потом между методом и сокетом, можно
встать самому. Мест для этого больше двадцати, у каждого своя область
видимости, и выбор места — это выбор того, что участнику будет видно.

## Дорога запроса

| Этап | Участник | Что видит | Чего не видит или не может |
|---|---|---|---|
| Контейнер | `Filter` | сырые запрос и ответ | контроллер, маршрут, аргументы |
| | `OncePerRequestFilter` | то же, но один раз на запрос | повторные проходы на `/error` и `forward` |
| | `FilterRegistrationBean` | пути, порядок, имя фильтра | — это регистрация, а не участник |
| Выбор метода | `version` в `@GetMapping` | версию API из заголовка | — |
| | `RequestCondition` | запрос целиком при выборе метода | аргументы: их ещё нет |
| | `HandlerInterceptor` | выбранный обработчик | тело: оно ещё не прочитано и сырым уже не будет |
| Аргументы | `Converter`, `ConverterFactory` | одно значение и два типа | запрос целиком |
| | `Formatter` по аннотации | значение, локаль, аннотацию на параметре | — |
| | `@InitBinder`, `@ModelAttribute` | связыватель и модель одного контроллера | другие контроллеры |
| | `HandlerMethodArgumentResolver` | запрос и описание параметра | другие аргументы метода |
| | `HttpMessageConverter`, Jackson | байты тела | конвертеры Spring из строки запроса |
| | `RequestBodyAdvice` | разобранное тело | результат проверки: её ещё не было |
| | `Validator`, `ConstraintValidator` | объект после связывания | — |
| | аспект `@Around` | все аргументы разом | проверку: она уже прошла |
| Результат | `HandlerMethodReturnValueHandler` | результат метода | ничего, если метод помечен `@ResponseBody` |
| | `ResponseBodyAdvice` | тело ответа до записи | — последний шанс добавить заголовок |
| | `ShallowEtagHeaderFilter` | готовое тело ответа | работу контроллера: она уже сделана |
| Ошибки | `@ExceptionHandler`, `ResponseEntityExceptionHandler` | исключение из метода | исключения из фильтров |
| | `HandlerExceptionResolver` | исключение и сырой ответ | — |
| Потоки | `CallableProcessingInterceptor` | рабочий поток асинхронного метода | — |

## Примеры

**Контейнер сервлетов**

| Класс | Что показывает |
|---|---|
| [`SanitizingFilter`](src/main/java/ru/sprbut/m28/filters/SanitizingFilter.java) | Тело читается один раз, поэтому изменить его можно только обёрткой |
| [`SanitizedRequest`](src/main/java/ru/sprbut/m28/filters/SanitizedRequest.java) | `HttpServletRequestWrapper`: делегирует всё, переопределяет два метода |
| [`CorrelationFilter`](src/main/java/ru/sprbut/m28/filters/CorrelationFilter.java) | `OncePerRequestFilter` не срабатывает второй раз на странице ошибки |
| [`AuditFilter`](src/main/java/ru/sprbut/m28/filters/AuditFilter.java) | `ContentCachingRequestWrapper` видит тело только после того, как его прочитал контроллер |
| [`LimitFilter`](src/main/java/ru/sprbut/m28/filters/LimitFilter.java) | Отказ из фильтра, переданный `HandlerExceptionResolver`, приходит как `ProblemDetail` |
| [`FiltersConfig`](src/main/java/ru/sprbut/m28/config/FiltersConfig.java) | `FilterRegistrationBean`: пути и порядок; `ShallowEtagHeaderFilter` на приветствии |

**Выбор метода**

| Класс | Что показывает |
|---|---|
| [`GreetingController`](src/main/java/ru/sprbut/m28/web/GreetingController.java) | Версии API из Spring 7: два метода на одном пути |
| [`ClientCondition`](src/main/java/ru/sprbut/m28/routing/ClientCondition.java) | Своё условие выбора метода по заголовку |
| [`ClientHandlerMapping`](src/main/java/ru/sprbut/m28/routing/ClientHandlerMapping.java) | Где таблица маршрутов спрашивает про свои условия |
| [`ClientRegistrations`](src/main/java/ru/sprbut/m28/routing/ClientRegistrations.java) | `WebMvcRegistrations`: подмена того, что создаёт автоконфигурация |
| [`DeviceController`](src/main/java/ru/sprbut/m28/web/DeviceController.java) | Метод с условием побеждает метод без условия |
| [`TimingInterceptor`](src/main/java/ru/sprbut/m28/interceptors/TimingInterceptor.java) | `preHandle` знает обработчик; почему заголовок пишется не в `postHandle` |

**Аргументы метода**

| Класс | Что показывает |
|---|---|
| [`LowerCaseConverter`](src/main/java/ru/sprbut/m28/converters/LowerCaseConverter.java) | Конвертер подбирается по паре типов — и в этом всё дело |
| [`LenientEnumFactory`](src/main/java/ru/sprbut/m28/converters/LenientEnumFactory.java) | Одна фабрика на все перечисления, без учёта регистра |
| [`PhoneFormatter`](src/main/java/ru/sprbut/m28/formatters/PhoneFormatter.java) | `Formatter`: разбор и печать с учётом локали |
| [`PhoneFormatterFactory`](src/main/java/ru/sprbut/m28/formatters/PhoneFormatterFactory.java) | Форматтер, собранный под атрибуты аннотации на параметре |
| [`ProfileController`](src/main/java/ru/sprbut/m28/web/ProfileController.java) | `@InitBinder` и `@ModelAttribute`: настройка одного контроллера |
| [`RoleGuard`](src/main/java/ru/sprbut/m28/validation/RoleGuard.java) | `Validator` Spring, подключённый к связывателю одного контроллера |
| [`CurrentUserArgumentResolver`](src/main/java/ru/sprbut/m28/handlers/CurrentUserArgumentResolver.java) | Свой источник аргумента: заголовок вместо пути и параметра |
| [`LoginDeserializer`](src/main/java/ru/sprbut/m28/json/LoginDeserializer.java) | У тела свой путь: `@JacksonComponent` вместо конвертера |
| [`MaskingRequestBodyAdvice`](src/main/java/ru/sprbut/m28/advices/MaskingRequestBodyAdvice.java) | Правка тела между разбором JSON и вызовом метода |
| [`NotReserved`](src/main/java/ru/sprbut/m28/validation/NotReserved.java) | Своё правило Bean Validation рядом с готовыми |
| [`TrimmingAspect`](src/main/java/ru/sprbut/m28/aspects/TrimmingAspect.java) | Аспект видит все аргументы, но приходит после проверки |
| [`SearchController`](src/main/java/ru/sprbut/m28/web/SearchController.java) | Единственный не-`final` класс: иначе аспекту не построить прокси |

**Результат и ответ**

| Класс | Что показывает |
|---|---|
| [`LinesReturnValueHandler`](src/main/java/ru/sprbut/m28/returns/LinesReturnValueHandler.java) | Свой тип результата — и почему в `@RestController` он не сработает |
| [`PlainController`](src/main/java/ru/sprbut/m28/web/PlainController.java) | Один результат, два ответа: с `@ResponseBody` и без |
| [`UserCsvConverter`](src/main/java/ru/sprbut/m28/messages/UserCsvConverter.java) | Конвертер сообщений и согласование по `Accept` |
| [`ElapsedResponseAdvice`](src/main/java/ru/sprbut/m28/advices/ElapsedResponseAdvice.java) | `ResponseBodyAdvice`: заголовок, пока ответ ещё не отправлен |

**Ошибки**

| Класс | Что показывает |
|---|---|
| [`Failures`](src/main/java/ru/sprbut/m28/web/Failures.java) | `ResponseEntityExceptionHandler`: `ProblemDetail` для исключений MVC даром |
| [`UnsupportedResolver`](src/main/java/ru/sprbut/m28/errors/UnsupportedResolver.java) | `HandlerExceptionResolver` — механизм, на котором стоит `@ExceptionHandler` |

**Контекст запроса**

| Класс | Что показывает |
|---|---|
| [`CorrelationConfig`](src/main/java/ru/sprbut/m28/context/CorrelationConfig.java) | `RequestContextHolder` и `@RequestScope` |
| [`TraceController`](src/main/java/ru/sprbut/m28/web/TraceController.java) | Три способа получить то, что положил фильтр, в том числе из другого потока |
| [`MdcPropagation`](src/main/java/ru/sprbut/m28/interceptors/MdcPropagation.java) | `CallableProcessingInterceptor`: MDC в рабочем потоке |
| [`HandlersConfig`](src/main/java/ru/sprbut/m28/config/HandlersConfig.java) | `WebMvcConfigurer` как перечень мест, куда можно встать |

## Ключевые выводы

* **`@Component` регистрирует не всех.** Фильтр подхватит контейнер сервлетов,
  конвертер и фабрику конвертеров — автоконфигурация MVC, обработчик
  исключений — сам диспетчер. А интерсептор, резолвер аргумента,
  обработчик результата и фабрику форматтеров по аннотации диспетчер
  берёт только из `WebMvcConfigurer`. Отсюда `HandlersConfig`.
* **`Converter<String, String>` не вызовется никогда.** `ConversionService`
  подбирает конвертер по паре «откуда, куда» и не преобразует тип в него же.
  Приведение к нижнему регистру начинает работать ровно тогда, когда
  у параметра появляется собственный тип — [`Login`](src/main/java/ru/sprbut/m28/dto/Login.java).
* **У тела запроса свой путь.** Конвертеры Spring работают для строки
  запроса, пути и заголовков; тело разбирает Jackson. Без
  `LoginDeserializer` тот же `Login` в JSON не разобрался бы вовсе.
* **Тело запроса читается один раз.** Фильтр, прочитавший поток и не
  подменивший запрос обёрткой, оставляет контроллеру пустое тело.
  `ContentCachingRequestWrapper` эту проблему не решает, а обходит: он
  запоминает тело, пока его читает кто-то другой, и до контроллера пуст.
* **`postHandle` для `@ResponseBody` опаздывает.** Конвертер сообщений
  отправляет ответ ещё внутри вызова метода, и настоящий Tomcat молча
  выбрасывает заголовки, выставленные после этого. `MockMvc` ошибку
  прячет; её ловит `HandlersAppTest` на живом сервере. Под
  `ShallowEtagHeaderFilter`, который придерживает тело, заголовок
  успевает — ещё один повод не полагаться на `postHandle`.
* **Свой конвертер сообщений встаёт в конец списка.** Поставленный вперёд
  (`addCustomConverter`), `UserCsvConverter` отдавал бы CSV всем, кто не
  прислал `Accept`.
* **Свой обработчик результата бессилен в `@RestController`.** Штатный
  обработчик `@ResponseBody` берётся за любой тип и стоит раньше.
* **`setAllowedFields` не защищает запись.** Запись связывается через
  конструктор, а список разрешённых полей действует только для сеттеров
  и полей. Роль, дописанную в форму, отвергает `RoleGuard`.
* **Совет над телом срабатывает до проверки, аспект — после.** `@Valid`
  смотрит на то, что вернул `afterBodyRead`, и не видит того, что потом
  сделает аспект.
* **Исключение из фильтра до `@ExceptionHandler` не доходит.** Советы
  над ошибками живут внутри диспетчера; `LimitFilter` передаёт отказ его
  `HandlerExceptionResolver` сам.
* **`extend…` дополняет, `configure…` заменяет.**
  `configureHandlerExceptionResolvers` выглядит безобидно, но выключает
  `@ExceptionHandler` вместе со всеми штатными ответами.
* **Spring переносит в рабочий поток только свои `ThreadLocal`.**
  `RequestContextHolder` и бины области запроса в `Callable` работают
  сами, MDC журнала — только с `MdcPropagation`.
* **`final` и прокси не дружат.** Конфигурация с `proxyBeanMethods`,
  контроллер под аспектом, бин области запроса, объявленный записью, —
  везде прокси строится наследованием, и `final` класс роняет контекст.
  Лечится там, где можно, без наследования: `proxyBeanMethods = false`,
  прокси по интерфейсу.

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
curl -i -H 'Accept: text/csv' -H 'Content-Type: application/json' \
  -d '{"username":"ivan","email":"ivan@example.com"}' localhost:8084/api/users
curl -i -H 'X-API-Version: 2' localhost:8084/api/greeting
curl -i -H 'X-Client: mobile' localhost:8084/api/device
curl -i 'localhost:8084/api/users/phone?number=8%20(912)%20345-67-89'
curl -i 'localhost:8084/api/users/role?role=admin'
curl -i -d 'name=Nina&role=ADMIN' localhost:8084/api/profiles
curl -i 'localhost:8084/api/search?query=%20books%20&city=%20Omsk'
curl -i -H 'X-Correlation-Id: demo' localhost:8084/api/trace/async/log
curl -i -X DELETE localhost:8084/api/users/ivan
curl -i localhost:8084/plain/users
```
