# Часть 30 — схема сервиса

[← к описанию части](README.md)

## Развёртывание

Два экземпляра сервиса из [`docker-compose.yaml`](docker-compose.yaml)
подключены к одному Redis. Любой экземпляр принимает и потоки, и отправку:
отправитель публикует уведомление в канал `sse-events`, а получают его
все экземпляры, включая самого отправителя.

Gateway в модуле нет, он показан пунктиром. В compose клиенты обращаются
к экземплярам напрямую через порты `8081` и `8082`. В настоящей системе
между ними стоит gateway, как в [части 29](../29-spring-cloud/README.md):
он приносит идентификатор пользователя.

```mermaid
flowchart LR
    browser["Клиент SSE<br/>вкладки, устройства"]
    sender["Отправитель событий"]
    gateway["Gateway<br/>часть 29, вне модуля"]

    subgraph compose["docker compose"]
        one["sse-one<br/>:8081"]
        two["sse-two<br/>:8082"]
        redis[("Redis 8<br/>pub/sub, канал sse-events")]
    end

    browser -.->|"GET /sse/stream/{user-id}"| gateway
    sender -.->|"POST /sse/send/user/{user-id}<br/>POST /sse/send/broadcast"| gateway
    gateway -.-> one
    gateway -.-> two

    one -->|PUBLISH| redis
    two -->|PUBLISH| redis
    redis -->|SUBSCRIBE| one
    redis -->|SUBSCRIBE| two

    classDef external stroke-dasharray: 5 5
    class gateway external
```

## Компоненты экземпляра

Сборка — в [`SseConfiguration`](src/main/java/ru/sprbut/m30/SseConfiguration.java).
`Audience` и `Bus` обёрнуты декораторами метрик. Уведомления `UserNotice`
и `BroadcastNotice` уходят в канал как JSON. Из канала они возвращаются
как `ParsedNotice` и разбираются только в момент доставки.

```mermaid
flowchart TB
    browser["Клиент SSE"]
    sender["Отправитель событий"]
    redis[("Redis<br/>канал sse-events")]

    subgraph instance["Экземпляр сервиса"]
        endpoint["SseEndpoint<br/>/sse, пинг раз в sse.heartbeat-timeout"]

        subgraph audience["Audience"]
            metered_audience["MeteredAudience"]
            sink_audience["SinkAudience<br/>ConcurrentMap&lt;Long, Crowd&gt;"]
            crowd["Crowd на пользователя<br/>Sinks.many().multicast().directBestEffort()"]
        end

        subgraph bus["Bus"]
            metered_bus["MeteredBus"]
            redis_bus["RedisBus<br/>ReactiveStringRedisTemplate"]
        end

        relay["Relay<br/>SmartLifecycle, переподписка с паузой sse.retry-pause"]
        registry["MeterRegistry"]
        actuator["/actuator/prometheus"]
    end

    browser -->|"GET /sse/stream/{user-id}"| endpoint
    sender -->|"POST /sse/send/user/{user-id}<br/>POST /sse/send/broadcast"| endpoint

    endpoint -->|"stream(user)"| metered_audience
    endpoint -->|"publish(UserNotice / BroadcastNotice)"| metered_bus

    metered_audience --> sink_audience
    sink_audience --> crowd

    metered_bus --> redis_bus
    redis_bus -->|"convertAndSend"| redis
    redis -->|"listenToChannel"| redis_bus

    relay -->|"notices()"| metered_bus
    relay -->|"ParsedNotice.deliver(audience)<br/>deliver / broadcast"| metered_audience

    metered_audience -->|"sse.server.*"| registry
    metered_bus -->|"redis.sse.*"| registry
    registry --> actuator
```
