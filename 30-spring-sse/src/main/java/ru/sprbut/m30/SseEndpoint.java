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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * HTTP-доступ к потокам событий пользователей; отправка — в {@link GrpcEndpoint}.
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
     * Период пинга, не дающего прокси закрыть молчащий поток.
     */
    private final Duration heartbeat;

    /**
     * Конструктор.
     * @param audience Подключённые пользователи
     * @param heartbeat Период пинга
     */
    public SseEndpoint(
        final Audience audience,
        @Value("${sse.heartbeat-timeout:20s}") final Duration heartbeat
    ) {
        this.audience = audience;
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
}
