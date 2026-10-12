/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Каналы, чьи подписки переживают обрыв: поток событий переподписывается
 * с растущей паузой, пока его не отменят.
 * @since 1.0
 */
public final class ResilientBus implements Bus {

    /**
     * Журнал.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ResilientBus.class);

    /**
     * Исходные каналы.
     */
    private final Bus origin;

    /**
     * Пауза перед первой переподпиской.
     */
    private final Duration pause;

    /**
     * Конструктор.
     * @param origin Исходные каналы
     * @param pause Пауза перед первой переподпиской
     */
    public ResilientBus(final Bus origin, final Duration pause) {
        this.origin = origin;
        this.pause = pause;
    }

    @Override
    public Mono<Long> publish(final long user, final Notice notice) {
        return this.origin.publish(user, notice);
    }

    @Override
    public Mono<Long> publish(final Notice notice) {
        return this.origin.publish(notice);
    }

    @Override
    public Flux<ServerSentEvent<String>> user(final long user) {
        return this.origin.user(user).retryWhen(
            this.retry(String.format("канал пользователя %d", user))
        );
    }

    @Override
    public Flux<ServerSentEvent<String>> broadcasts() {
        return this.origin.broadcasts().retryWhen(this.retry("общий канал"));
    }

    private Retry retry(final String channel) {
        return Retry.backoff(Long.MAX_VALUE, this.pause)
            .maxBackoff(this.pause.multipliedBy(30L))
            .doBeforeRetry(
                signal -> ResilientBus.LOG.warn(
                    String.format(
                        "Подписка на %s потеряна, попытка восстановления номер %d",
                        channel, signal.totalRetries() + 1L
                    ),
                    signal.failure()
                )
            );
    }
}
