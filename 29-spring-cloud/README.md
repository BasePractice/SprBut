# Часть 29 — система сервисов на Spring Cloud

[← к списку модулей](../README.md) · [модуль 23 «Spring Cloud»](../23-spring-cloud/README.md)

## О чём эта часть

Модуль 23 показывает инструменты Spring Cloud по одному, в одном приложении
и с реестром из настроек. Эта часть собирает из них настоящую систему:
семь модулей, пять процессов с сетью между ними, сервер настроек, Eureka,
Postgres и единственная дверь наружу — gateway. Главный вопрос здесь уже
не «как позвать соседа», а «кому из соседей и в чём можно верить».

## Сервисы

| Модуль | Порт | Что делает |
|---|---|---|
| [`discovery`](discovery/) | 8761 | Реестр Eureka: сервисы объявляют в нём адреса и находят друг друга по имени |
| [`config`](config/) | 8888 | Config Server в профиле `native`: настройки всех сервисов лежат в [`configs/`](config/src/main/resources/configs/) |
| [`parameters`](parameters/) | 8081 | Параметры по ключу (владелец, имя), внутренний сервис без защиты |
| [`auth`](auth/) | 8082 | Учётки, вход, выпуск, обмен и отзыв токенов, JWKS |
| [`profile`](profile/) | 8083 | Профили пользователей и их характеристики |
| [`gateway`](gateway/) | 8080 | Единственный порт наружу: проверка токена, заголовки пользователя, `/me` |
| [`identity`](identity/) | — | Библиотека: пользователь, переданный gateway в заголовках `X-User-*` |

## Как устроено доверие

Токен проверяет только gateway. Подпись RS256 он сверяет локально по ключам
из `/.well-known/jwks.json` сервиса auth, а у auth спрашивает лишь одно —
жива ли сессия токена. Ответ кэшируется на 10 секунд: отозванный токен
перестаёт работать не мгновенно, но и auth не отвечает на каждый запрос.

Дальше токен не идёт. Gateway снимает `Authorization` и пришедшие снаружи
`X-User-*` и ставит свои `X-User-Id`, `X-User-Role`, `X-User-Login`.
Сервисы за ним строят аутентификацию из этих заголовков
([`HeaderAuthentication`](identity/src/main/java/ru/sprbut/m29/identity/HeaderAuthentication.java))
и токен не видят вовсе. Это безопасно ровно до тех пор, пока наружу открыт
один gateway: порт любого другого сервиса — это дверь, в которую можно
войти кем угодно.

По той же причине gateway сам закрывает методы, которые сервисы держат
друг для друга: `DELETE /auth/users/{id}` зовёт profile при удалении
пользователя, `POST /profiles` — auth при регистрации. Пропусти их gateway,
и пользователь удалил бы учётку в обход профиля, оставив профиль
и характеристики без хозяина.

## Примеры

| Класс | Что показывает |
|---|---|
| [`ActiveTokens`](gateway/src/main/java/ru/sprbut/m29/gateway/ActiveTokens.java) | Декоратор над `ReactiveJwtDecoder`: подпись проверяет Nimbus, жизнь сессии — auth |
| [`CachedSessions`](gateway/src/main/java/ru/sprbut/m29/gateway/CachedSessions.java) | Асинхронный кэш Caffeine над ответом auth |
| [`IdentityHeaders`](gateway/src/main/java/ru/sprbut/m29/gateway/IdentityHeaders.java) | `GlobalFilter`, который заменяет токен заголовками пользователя |
| [`MeEndpoint`](gateway/src/main/java/ru/sprbut/m29/gateway/MeEndpoint.java) | `Mono.zip`: профиль и параметры запрашиваются параллельно, сбой любого — 502 |
| [`PgSessions`](auth/src/main/java/ru/sprbut/m29/auth/PgSessions.java) | Refresh-токен хранится хешем; ротация в транзакции с `FOR UPDATE`, повтор старого закрывает сессию |
| [`SigningKey`](auth/src/main/java/ru/sprbut/m29/auth/SigningKey.java) | Ключ RS256 из PKCS#8; открытая часть и `kid` выводятся из закрытой |
| [`RegistrationEndpoint`](auth/src/main/java/ru/sprbut/m29/auth/RegistrationEndpoint.java) | Учётка в auth, профиль в profile; если профиль не создался, учётка удаляется |
| [`ProfilesEndpoint`](profile/src/main/java/ru/sprbut/m29/profile/ProfilesEndpoint.java) | Удаление пользователя по трём сервисам: характеристики, учётка, профиль |
| [`Wiring`](profile/src/main/java/ru/sprbut/m29/profile/Wiring.java) | `@LoadBalanced` построитель рядом с обычным `@Primary` |

## API через gateway

| Метод | Путь | Доступ |
|---|---|---|
| POST | `/auth/register` `{login,password,name}` | все, роль всегда USER |
| POST | `/auth/login` `{login,password}` | все |
| POST | `/auth/refresh` `{refresh_token}` — старый гаснет, выдаётся новая пара | все |
| POST | `/auth/logout` `{refresh_token}` | с токеном, своя сессия |
| GET | `/auth/users/{id}/sessions` | сам или ADMIN |
| POST | `/auth/users/{id}/revoke` | сам или ADMIN |
| PUT | `/auth/users/{id}/password` `{password}` — закрывает все сессии | сам или ADMIN |
| PUT | `/auth/users/{id}/role` `{role}` — закрывает все сессии | ADMIN |
| GET | `/me` | с токеном |
| GET | `/profiles` | ADMIN |
| GET, PUT, DELETE | `/profiles/{id}` | сам или ADMIN |
| GET, PUT | `/profiles/{id}/parameters`, `/profiles/{id}/parameters/{name}` (`text/plain`) | сам или ADMIN |

Access-токен живёт час, refresh — 30 дней. Первый администратор заводится
миграциями Flyway в auth и profile с фиксированным идентификатором
`00000000-0000-0000-0000-000000000001`, хеш его пароля приходит из окружения.

## Подводные камни

* `@Repository` вешает CGLIB-прокси перевода исключений и падает на `final`
  классе. Здесь стоит `@Component`: `JdbcClient` и сам переводит `SQLException`,
  например в `DuplicateKeyException`.
* Балансируемый построитель клиента подхватывает и сам Eureka-клиент — и тот
  ищет реестр в реестре. Поэтому в каждом `Wiring` есть ещё обычный
  `@Primary` построитель, а балансируемый перед настройкой клонируется:
  он один на все клиенты сервиса.
* `text/plain` без charset Spring пишет в ISO-8859-1: клиент шлёт
  `text/plain;charset=UTF-8`, а GET-методы объявляют его в `produces`.
* Тестовый `application.yaml` заслоняет основной, чтобы в тестах не было
  `spring.config.import=configserver:`.
* После одновременного рестарта сервисов до полуминуты возможны 503
  «No instances available»: так живёт кэш реестра Eureka.

## Ключевые выводы

* Проверять токен в каждом сервисе — значит раздать каждому ключи и правила.
  Проверять его на входе — значит верить сети внутри. Здесь выбрано второе,
  и вся безопасность держится на том, что дверь одна.
* Подписанный токен нельзя отозвать: он живёт до `exp`. Отзыв появляется только
  вместе с вопросом к тому, кто токен выпустил, и цена этого вопроса снижается
  кэшем — ценой нескольких секунд жизни после отзыва.
* Ротация refresh-токена превращает его кражу в заметное событие: старый токен
  повторно предъявит либо вор, либо хозяин, и сессия закрывается для обоих.
* Транзакция не переходит границу сервиса. Регистрация и удаление проходят
  по двум-трём базам, и согласованность здесь — забота кода, который откатывает
  сделанное руками.

## Запуск

Тесты не требуют ни Postgres, ни соседей — базы и сервисы подменены фейками:

```bash
mvn -pl 29-spring-cloud -amd test
```

Стенд целиком поднимается в Docker. Учебные ключ подписи и хеш пароля `admin`
лежат в [`.env.example`](.env.example):

```bash
cp 29-spring-cloud/.env.example 29-spring-cloud/.env
mvn -pl 29-spring-cloud -amd -DskipTests package
docker compose -f 29-spring-cloud/docker-compose.yaml up -d --build
```

Сквозной сценарий — [`http/cloud.http`](http/cloud.http), окружение `docker`
в IntelliJ HTTP Client. Без IDE, из каталога модуля:

```bash
docker run --rm --network 29-spring-cloud_default -v "$PWD/http":/workdir jetbrains/intellij-http-client --env-file http-client.env.json --env docker -V gateway=http://gateway:8080 -V discovery=http://discovery:8761 cloud.http
```
