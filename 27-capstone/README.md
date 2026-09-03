# Модуль 27 — Итоговое задание: SprBut Tracker

[Слайды 280–285 («Итоги»)](https://docs.google.com/presentation/d/1zJBrQvw25ehkCVgjoY5U0-Gap6JR3ogs/edit#slide=id.g3f87e69302f_0_3) ·
[← к списку модулей](../README.md)

## Задание

> Построить трекер задач, в котором встречается **каждая** тема курса, и снабдить его
> механизмом самообъяснения: приложение должно уметь показать, во что контейнер
> превратил его собственные классы.

Формулировка задания намеренно требует не «применить Spring», а **предъявить разницу
между исходниками и рантаймом**. Вся первая половина курса — рефлексия, аннотации,
генерация кода — существует ради того, чтобы эту разницу создать; вся вторая —
чтобы ею управлять.

### Что должно быть в решении

1. Доменная модель с правилами, живущими в самой сущности.
2. Своя аннотация и свой аспект, которые превращают метку в поведение.
3. Внедрение через конструктор, включая внедрение времени.
4. Внешняя конфигурация с профилями и неизменяемой привязкой.
5. Собственная автоконфигурация, отступающая перед бином приложения.
6. Веб-слой с проверкой запросов и переводом ошибок домена в коды HTTP.
7. Защита: цепочка фильтров перед диспетчером, роль на адресе и правило на методе.
8. Тот же ресурс, отданный потоком, а не списком.
9. Сосед по сети, который может не ответить, — и предохранитель на этот случай.
10. Генерация кода на этапе компиляции: Lombok и MapStruct.
11. Подсказки для native image.
12. Тесты: полный контекст, срезы, юнит-тесты без контейнера.
13. **Карта контейнера** — итоговый расширенный пример.

## Как темы курса легли в код

| Тема курса | Где в модуле |
|---|---|
| 01–04 Reflection | [`ContextMap`](src/main/java/ru/sprbut/m27/extended/ContextMap.java) читает классы бинов и снимает прокси |
| 02 JavaBeans | [`TaskViews`](src/main/java/ru/sprbut/m27/web/TaskViews.java) — цена соглашения `getXxx` для инструментов |
| 05–06 Аннотации | [`Audited`](src/main/java/ru/sprbut/m27/audit/Audited.java): `RUNTIME`, `@Target(METHOD)`, элемент `value` |
| 07–09 APT | [`TaskViews`](src/main/java/ru/sprbut/m27/web/TaskViews.java) — реализации нет в исходниках, её пишет компилятор |
| 10 Lombok, MapStruct | [`TaskView`](src/main/java/ru/sprbut/m27/web/TaskView.java) на `@Value`/`@Builder` против `record` в [`NewTaskRequest`](src/main/java/ru/sprbut/m27/web/NewTaskRequest.java) |
| 11–12 IoC и DI | [`TaskService`](src/main/java/ru/sprbut/m27/service/TaskService.java): четыре зависимости через конструктор, включая `Clock` |
| 13–14 Bean и жизненный цикл | [`AuditTrail`](src/main/java/ru/sprbut/m27/audit/AuditTrail.java) — singleton, переживающий все запросы |
| 15 Spring AOP | [`AuditAspect`](src/main/java/ru/sprbut/m27/audit/AuditAspect.java) и ловушка `getMostSpecificMethod` |
| 16 Конфигурация | [`TrackerProperties`](src/main/java/ru/sprbut/m27/config/TrackerProperties.java), профиль `demo` |
| 17 Аннотации Spring | [`TaskController`](src/main/java/ru/sprbut/m27/web/TaskController.java), [`Failures`](src/main/java/ru/sprbut/m27/web/Failures.java) |
| 18 Запуск | [`TrackerRunner`](src/main/java/ru/sprbut/m27/startup/TrackerRunner.java) — `ApplicationRunner` |
| 19 Автоконфигурация | [`ClockAutoConfiguration`](src/main/java/ru/sprbut/m27/autoconfigure/ClockAutoConfiguration.java) + `AutoConfiguration.imports` |
| 20 Spring MVC | [`TaskController`](src/main/java/ru/sprbut/m27/web/TaskController.java) и таблица маршрутов, снятая с самого приложения |
| 21 WebFlux | [`TaskFeed`](src/main/java/ru/sprbut/m27/web/TaskFeed.java) — `Flux` в сервлетном приложении и честная цена этого |
| 22 Spring Security | [`TrackerSecurity`](src/main/java/ru/sprbut/m27/security/TrackerSecurity.java): роль на адресе, `@PreAuthorize` на закрытии задачи |
| 23 Spring Cloud | [`Board`](src/main/java/ru/sprbut/m27/remote/Board.java) — сосед по имени, декларативный клиент, предохранитель |
| 24 Тестирование | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, `@TestConfiguration`, `@WithMockUser` |
| 25 Типичные ошибки | `@ConditionalOnMissingBean` вне автоконфигурации и столкновение имён бинов — разобрано ниже |
| 26 AOT | [`TrackerHints`](src/main/java/ru/sprbut/m27/aot/TrackerHints.java) для DTO, которые собирает Jackson |

## Расширенный пример: карта контейнера

[`ContextMap`](src/main/java/ru/sprbut/m27/extended/ContextMap.java) — приложение,
которое рассказывает о себе само.

```java
map.cards();               // имя бина, настоящий класс, область видимости, @Audited-методы
map.proxy("taskService");  // jdk
map.proxied("auditTrail"); // false
map.routes();              // POST /api/tasks, GET /api/tasks/stream, ...
map.filters();             // ...BasicAuthenticationFilter, ..., AuthorizationFilter
```

Доступно по HTTP — под ролью администратора: `GET /api/introspection/beans`,
`/routes`, `/filters` и `/audit`.

В одном классе сходится весь курс. Рефлексия читает настоящие классы бинов и ищет
в них аннотации. `Audited` оказывается всего лишь меткой, которую кто-то должен
прочесть. Контейнер отдаёт определения бинов и области видимости. `AopProxyUtils`
снимает обёртку и показывает: **бин в контексте — не тот объект, что написан
в исходниках**. Именно это и есть ответ на вопрос, ради которого затевался курс.

Новые темы курса ложатся сюда же.
Таблица маршрутов — это `@GetMapping` и `@PostMapping`, прочитанные при старте:
«запрос ушёл не туда» — вопрос к ней, а не к исходникам. Цепочка фильтров — это
то, во что превратилась конфигурация защиты: «почему 401 вместо 403» — вопрос
к порядку в этом списке. Обе таблицы отвечают тем же самым: аннотацию давно
прочли, работает результат чтения.

## Грабли, на которые модуль наступил при сборке

Все оставлены в коде вместе с разбором — они полезнее любого примера.

**`@ConditionalOnMissingBean` в обычной `@Configuration` не работает.** Условие
проверяет бины, зарегистрированные *к моменту проверки*, а обычные конфигурации
разбираются в произвольном порядке. Тест с `@TestConfiguration` получал
`BeanDefinitionOverrideException` вместо подмены. Лечение — вынести бин
в настоящую автоконфигурацию: Boot обрабатывает их последними, и приём наконец
начинает работать так, как описан на слайде.

**Boot по умолчанию форсирует CGLIB.** `spring.aop.proxy-target-class` равен `true`,
поэтому `final`-класс с интерфейсом не проксируется вовсе: «Could not generate CGLIB
subclass». В [`application.yaml`](src/main/resources/application.yaml) поведение
возвращено к тому, что описывает слайд: есть интерфейс — JDK-прокси, нет — CGLIB.

**JDK-прокси прячет аннотации реализации.** `MethodSignature.getMethod()` отдаёт метод
*интерфейса*, где никакого `@Audited` нет — аспект молча писал в журнал `open` вместо
`task.open`. Спасает `AopUtils.getMostSpecificMethod`.

**Имя бина берётся из имени метода, а не из типа.** `@Service` над классом `Board`
и `@Bean BoardApi board(...)` в соседней конфигурации дают одно имя `board`
на два разных бина, и контекст не поднимается вовсе:
`BeanDefinitionOverrideException`. Метод переименован в `notices` — то же самое,
что случается с двумя `@Bean`-методами одного имени в разных конфигурациях.

**`RequestMappingHandlerMapping` в приложении не один.** Actuator заводит второй
такой же бин для своих эндпоинтов, и выбор по типу становится неоднозначным:
`NoUniqueBeanDefinitionException`. Карта берёт реестр по имени — тот случай,
когда имя бина оказывается частью контракта, а не деталью.

## Ключевые выводы

* Аннотация — метаданные, а не поведение. `@Audited` без аспекта, `@Transactional`
  без прокси и `@PostConstruct` вне контейнера не делают ровно ничего.
* Бин в контексте — не тот объект, что в исходниках. Пока это не увидено
  своими глазами, поведение прокси выглядит магией.
* Порядок обработки конфигураций — не деталь реализации, а часть контракта:
  на нём стоит вся автоконфигурация.
* Время, случайность и текущий пользователь — такие же зависимости, как репозиторий.
  Внедрённый `Clock` превращает хрупкий тест в обычный.
* Решение пустить запрос принимается в фильтрах, до `DispatcherServlet`.
  В контроллерах трекера нет ни одной проверки прав — и это следствие, а не упущение.
* Правило на адресе защищает путь, правило на методе — операцию. `@PreAuthorize`
  на закрытии задачи держится и тогда, когда до метода ведёт не HTTP.
* `Flux` в ответе не делает приложение реактивным: за ним остаётся блокирующий JDBC.
  Реактивен здесь ответ, а не работа с данными, и разница между модулями 20 и 21
  проходит ровно по этой линии.
* Отказ соседа — нормальный режим работы, а не авария. Закрытие задачи не отменяется
  оттого, что доска молчит: в журнале появляется `board:offline`, и на этом всё.

## Запуск

```bash
mvn -pl 27-capstone test
mvn -pl 27-capstone spring-boot:run
```

Пользователей двое: `anna` с паролем `anna-pass` (роль `USER`) и `boris`
с паролем `boris-pass` (роли `USER` и `ADMIN`).

```bash
curl -u anna:anna-pass -X POST localhost:8080/api/tasks \
  -H 'Content-Type: application/json' -d '{"title":"написать отчёт"}'
curl -u anna:anna-pass localhost:8080/api/tasks/stream   # тот же ресурс потоком
curl -u boris:boris-pass localhost:8080/api/introspection/beans
curl -u boris:boris-pass localhost:8080/api/introspection/routes
curl -u boris:boris-pass localhost:8080/api/introspection/filters
curl localhost:8080/actuator/health        # единственный открытый адрес
```

Соседнего сервиса на `tracker-board` в репозитории нет, и это намеренно:
закрытие задачи всё равно проходит, а в `/api/introspection/audit` появляется
запись `board:offline`.
