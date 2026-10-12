# Часть 30 — схема сервиса

[← к описанию части](README.md)

## Развёртывание

Два экземпляра сервиса из [`docker-compose.yaml`](docker-compose.yaml)
подключены к одному Redis. Любой экземпляр отдаёт потоки по HTTP
и принимает отправку по gRPC. У каждого пользователя свой канал
`sse:user:{user-id}`, на который подписан только тот экземпляр, где у этого
пользователя открыт поток. На общий канал `sse:broadcast` подписаны все
экземпляры с момента запуска.

Gateway в модуле нет, он показан пунктиром. В compose клиенты обращаются
к экземплярам напрямую: HTTP на портах `8081` и `8082`, gRPC на `9091`
и `9092`. В настоящей системе между ними стоит gateway, как в
[части 29](../29-spring-cloud/README.md): он приносит идентификатор пользователя.

```mermaid
flowchart LR
    browser["Клиент SSE<br/>вкладки, устройства"]
    sender["Отправитель событий"]
    gateway["Gateway<br/>часть 29, вне модуля"]

    subgraph compose["docker compose"]
        one["sse-one<br/>HTTP :8081, gRPC :9091"]
        two["sse-two<br/>HTTP :8082, gRPC :9092"]
        redis[("Redis 8<br/>sse:user:{user-id}<br/>sse:broadcast")]
    end

    browser -.->|"GET /sse/stream/{user-id}"| gateway
    sender -.->|"gRPC Notifier/SendToUser<br/>gRPC Notifier/Broadcast"| gateway
    gateway -.-> one
    gateway -.-> two

    one -->|"PUBLISH"| redis
    two -->|"PUBLISH"| redis
    redis -->|"SUBSCRIBE: свои пользователи + broadcast"| one
    redis -->|"SUBSCRIBE: свои пользователи + broadcast"| two

    classDef external stroke-dasharray: 5 5
    class gateway external
```

## Адресная и широковещательная доставка

Личное уведомление получает только экземпляр с подключённым пользователем.
Общее получают все экземпляры, и каждый рассылает его своим пользователям.

```mermaid
sequenceDiagram
    participant C as Клиент SSE
    participant A as sse-one
    participant R as Redis
    participant B as sse-two
    participant S as Отправитель

    C->>A: GET /sse/stream/42
    A->>R: SUBSCRIBE sse:user:42 (первый поток пользователя)
    S->>B: SendToUser(user_id=42)
    B->>R: PUBLISH sse:user:42
    R-->>B: 1 получатель
    B-->>S: Accepted(receivers=1)
    R->>A: сообщение из sse:user:42
    A->>C: event: user-event

    S->>B: Broadcast
    B->>R: PUBLISH sse:broadcast
    R->>A: сообщение из sse:broadcast
    R->>B: сообщение из sse:broadcast
    A->>C: event: broadcast (всем пользователям sse-one)

    C--xA: поток закрыт
    A->>R: UNSUBSCRIBE sse:user:42 (последний поток пользователя)
```

## Компоненты экземпляра

Сборка — в [`SseConfiguration`](src/main/java/ru/sprbut/m30/SseConfiguration.java).
`Audience` и `Bus` обёрнуты декораторами. Все подписки идут через один
`ReactiveRedisMessageListenerContainer`, то есть через одно соединение Redis.
Публикация идёт через `ReactiveStringRedisTemplate`: соединение в режиме
подписки других команд не принимает. Уведомления уходят в канал как JSON
`{"event", "text"}`, а возвращаются как `ParsedNotice`: битое пропускается,
подписку оно не рвёт.

Работа идёт в двух пулах из `sse.pools.*`. `ScheduledBus` переносит события
подписок из потока соединения Redis в пул `sse.delivery`, а сервер gRPC
выполняет вызовы в пуле `grpc.commands`, который отдаёт ему `GrpcExecutor`.
Метрики обоих пулов — `executor.*` с тегом `name`.

```mermaid
flowchart TB
    browser["Клиент SSE"]
    sender["Отправитель событий"]
    redis[("Redis")]

    subgraph instance["Экземпляр сервиса"]
        endpoint["SseEndpoint<br/>/sse, пинг раз в sse.heartbeat-timeout"]
        grpc["GrpcEndpoint<br/>Notifier, порт 9090"]
        commands["GrpcExecutor<br/>пул grpc.commands"]

        subgraph audience["Audience"]
            metered_audience["MeteredAudience"]
            sink_audience["SinkAudience<br/>ConcurrentMap&lt;Long, Crowd&gt;"]
            crowd["Crowd на пользователя<br/>Sinks.many().multicast().directBestEffort()<br/>+ подписка на sse:user:{user-id}"]
        end

        subgraph bus["Bus"]
            metered_bus["MeteredBus"]
            resilient_bus["ResilientBus<br/>переподписка с паузой sse.retry-pause"]
            scheduled_bus["ScheduledBus<br/>publishOn в пул sse.delivery"]
            redis_bus["RedisBus"]
            container["ReactiveRedisMessageListenerContainer<br/>одно соединение"]
            template["ReactiveStringRedisTemplate"]
        end

        relay["Relay<br/>SmartLifecycle, подписка на sse:broadcast"]
        registry["MeterRegistry"]
        actuator["/actuator/prometheus"]
    end

    browser -->|"GET /sse/stream/{user-id}"| endpoint
    sender -->|"SendToUser / Broadcast"| commands
    commands -->|"вызов в потоке пула"| grpc

    endpoint -->|"stream(user)"| metered_audience
    grpc -->|"publish(user, notice) / publish(notice)"| metered_bus

    metered_audience --> sink_audience
    sink_audience --> crowd
    crowd -->|"user(user-id)"| metered_bus

    metered_bus --> resilient_bus
    resilient_bus --> scheduled_bus
    scheduled_bus --> redis_bus
    redis_bus --> container
    redis_bus --> template
    template -->|"PUBLISH"| redis
    container <-->|"SUBSCRIBE / UNSUBSCRIBE"| redis

    relay -->|"broadcasts()"| metered_bus
    relay -->|"broadcast(event)"| metered_audience

    metered_audience -->|"sse.server.*"| registry
    metered_bus -->|"redis.sse.*"| registry
    commands -->|"executor.*"| registry
    scheduled_bus -->|"executor.*"| registry
    registry --> actuator
```
