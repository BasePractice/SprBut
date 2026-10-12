/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m30;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * Каналы, чьи события доставляются в отдельном пуле потоков.
 *
 * <p>Сообщения всех подписок приходят в одном потоке соединения Redis.
 * Если доставлять их в нём же, медленная толпа задержит события всех
 * остальных пользователей. Здесь события каждой подписки уходят в пул,
 * сохраняя свой порядок, а подписки разных пользователей доставляются
 * параллельно. Когда пул переполнен, подписка обрывается ошибкой,
 * и её восстанавливает {@link ResilientBus}.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor
public final class ScheduledBus implements Bus {

    /**
     * Исходные каналы.
     */
    private final Bus origin;

    /**
     * Пул доставки.
     */
    private final Scheduler scheduler;

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
        return this.origin.user(user).publishOn(this.scheduler);
    }

    @Override
    public Flux<ServerSentEvent<String>> broadcasts() {
        return this.origin.broadcasts().publishOn(this.scheduler);
    }
}
