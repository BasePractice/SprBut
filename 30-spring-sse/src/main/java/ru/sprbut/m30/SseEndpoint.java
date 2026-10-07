/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * HTTP-доступ к потокам событий пользователей и к отправке уведомлений.
 * @since 1.0
 */
@RestController
@RequestMapping("/sse")
public final class SseEndpoint {

    /**
     * Подключённые пользователи.
     */
    private final Audience audience;

    /**
     * Канал уведомлений.
     */
    private final Bus bus;

    /**
     * Период пинга, не дающего прокси закрыть молчащий поток.
     */
    private final Duration heartbeat;

    /**
     * Конструктор.
     * @param audience Подключённые пользователи
     * @param bus Канал уведомлений
     * @param heartbeat Период пинга
     */
    public SseEndpoint(
        final Audience audience,
        final Bus bus,
        @Value("${sse.heartbeat-timeout:20s}") final Duration heartbeat
    ) {
        this.audience = audience;
        this.bus = bus;
        this.heartbeat = heartbeat;
    }

    /**
     * Поток событий пользователя с пингами.
     * @param user Идентификатор пользователя
     * @return События
     */
    @GetMapping(path = "/stream/{user-id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@PathVariable("user-id") final long user) {
        return Flux.merge(
            this.audience.stream(user),
            Flux.interval(this.heartbeat).map(
                tick -> ServerSentEvent.<String>builder().comment("ping").build()
            )
        );
    }

    /**
     * Отправить уведомление пользователю на всех экземплярах.
     * @param user Идентификатор пользователя
     * @param text Текст
     * @return Завершение отправки в канал
     */
    @PostMapping("/send/user/{user-id}")
    public Mono<Void> user(
        @PathVariable("user-id") final long user,
        @RequestBody final String text
    ) {
        return this.bus.publish(new UserNotice(user, text));
    }

    /**
     * Отправить уведомление всем пользователям на всех экземплярах.
     * @param text Текст
     * @return Завершение отправки в канал
     */
    @PostMapping("/send/broadcast")
    public Mono<Void> broadcast(@RequestBody final String text) {
        return this.bus.publish(new BroadcastNotice(text));
    }
}
