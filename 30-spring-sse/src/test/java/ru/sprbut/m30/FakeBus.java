/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Канал в памяти: всё отправленное сразу приходит подписчикам, а первые
 * подписки могут обрываться ошибкой.
 * @since 1.0
 */
final class FakeBus implements Bus {

    /**
     * Источник уведомлений.
     */
    private final Sinks.Many<Notice> sink;

    /**
     * Сколько подписок ещё оборвать.
     */
    private final AtomicInteger breaks;

    /**
     * Конструктор надёжного канала.
     */
    FakeBus() {
        this(0);
    }

    /**
     * Конструктор канала, обрывающего первые подписки.
     * @param breaks Сколько подписок оборвать
     */
    FakeBus(final int breaks) {
        this(Sinks.many().replay().all(), new AtomicInteger(breaks));
    }

    /**
     * Конструктор.
     * @param sink Источник уведомлений
     * @param breaks Сколько подписок оборвать
     */
    FakeBus(final Sinks.Many<Notice> sink, final AtomicInteger breaks) {
        this.sink = sink;
        this.breaks = breaks;
    }

    @Override
    public Mono<Void> publish(final Notice notice) {
        return Mono.fromRunnable(
            () -> this.sink.emitNext(notice, Sinks.EmitFailureHandler.FAIL_FAST)
        );
    }

    @Override
    public Flux<Notice> notices() {
        return Flux.defer(
            () -> {
                final Flux<Notice> flux;
                if (this.breaks.getAndDecrement() > 0) {
                    flux = Flux.error(
                        new UncheckedIOException(new IOException("Канал оборван намеренно"))
                    );
                } else {
                    flux = this.sink.asFlux();
                }
                return flux;
            }
        );
    }
}
