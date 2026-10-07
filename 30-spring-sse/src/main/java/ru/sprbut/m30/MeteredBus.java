/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Канал, чьи отправленные и полученные уведомления считаются в метриках.
 * @since 1.0
 */
public final class MeteredBus implements Bus {

    /**
     * Исходный канал.
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
     * @param origin Исходный канал
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

    /**
     * Конструктор.
     * @param origin Исходный канал
     * @param published Счётчик отправленных уведомлений
     * @param received Счётчик полученных уведомлений
     */
    MeteredBus(final Bus origin, final Counter published, final Counter received) {
        this.origin = origin;
        this.published = published;
        this.received = received;
    }

    @Override
    public Mono<Void> publish(final Notice notice) {
        return this.origin.publish(notice).doOnSuccess(done -> this.published.increment());
    }

    @Override
    public Flux<Notice> notices() {
        return this.origin.notices().doOnNext(notice -> this.received.increment());
    }
}
