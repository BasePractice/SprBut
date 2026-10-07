/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Пользователи, чьи подключения и полученные события считаются в метриках.
 * @since 1.0
 */
public final class MeteredAudience implements Audience {

    /**
     * Исходные пользователи.
     */
    private final Audience origin;

    /**
     * Число открытых потоков.
     */
    private final AtomicInteger active;

    /**
     * Счётчик открытых за всё время потоков.
     */
    private final Counter opened;

    /**
     * Счётчик отправленных событий.
     */
    private final Counter sent;

    /**
     * Конструктор, регистрирующий метрики в реестре.
     * @param origin Исходные пользователи
     * @param registry Реестр метрик
     */
    public MeteredAudience(final Audience origin, final MeterRegistry registry) {
        this(
            origin,
            registry.gauge("sse.server.active.connections", new AtomicInteger(0)),
            Counter.builder("sse.server.connections.opened")
                .description("Сколько всего SSE-подключений открыто")
                .register(registry),
            Counter.builder("sse.server.events.sent")
                .description("Сколько всего SSE-событий отправлено")
                .register(registry)
        );
    }

    /**
     * Конструктор.
     * @param origin Исходные пользователи
     * @param active Число открытых потоков
     * @param opened Счётчик открытых потоков
     * @param sent Счётчик отправленных событий
     */
    MeteredAudience(
        final Audience origin,
        final AtomicInteger active,
        final Counter opened,
        final Counter sent
    ) {
        this.origin = origin;
        this.active = active;
        this.opened = opened;
        this.sent = sent;
    }

    @Override
    public Flux<ServerSentEvent<String>> stream(final long user) {
        return this.origin.stream(user)
            .doOnSubscribe(
                subscription -> {
                    this.opened.increment();
                    this.active.incrementAndGet();
                }
            )
            .doOnNext(event -> this.sent.increment())
            .doFinally(signal -> this.active.decrementAndGet());
    }

    @Override
    public void deliver(final long user, final ServerSentEvent<String> event) {
        this.origin.deliver(user, event);
    }

    @Override
    public void broadcast(final ServerSentEvent<String> event) {
        this.origin.broadcast(event);
    }
}
