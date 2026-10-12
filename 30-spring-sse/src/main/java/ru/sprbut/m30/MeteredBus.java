/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Каналы, чьи отправленные и полученные уведомления считаются в метриках.
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MeteredBus implements Bus {

    /**
     * Исходные каналы.
     */
    private final Bus origin;

    /**
     * Счётчик отправленных уведомлений.
     */
    private final Counter published;

    /**
     * Счётчик полученных уведомлений.
     */
    private final Counter received;

    /**
     * Конструктор, регистрирующий метрики в реестре.
     * @param origin Исходные каналы
     * @param registry Реестр метрик
     */
    public MeteredBus(final Bus origin, final MeterRegistry registry) {
        this(
            origin,
            Counter.builder("redis.sse.publish.total")
                .description("Сколько всего сообщений опубликовано в Redis")
                .register(registry),
            Counter.builder("redis.sse.receive.total")
                .description("Сколько всего сообщений получено из Redis")
                .register(registry)
        );
    }

    @Override
    public Mono<Long> publish(final long user, final Notice notice) {
        return this.origin.publish(user, notice).doOnSuccess(count -> this.published.increment());
    }

    @Override
    public Mono<Long> publish(final Notice notice) {
        return this.origin.publish(notice).doOnSuccess(count -> this.published.increment());
    }

    @Override
    public Flux<ServerSentEvent<String>> user(final long user) {
        return this.origin.user(user).doOnNext(event -> this.received.increment());
    }

    @Override
    public Flux<ServerSentEvent<String>> broadcasts() {
        return this.origin.broadcasts().doOnNext(event -> this.received.increment());
    }
}
