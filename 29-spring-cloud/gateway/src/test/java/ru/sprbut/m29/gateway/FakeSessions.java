/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Mono;

/**
 * Сессии для тестов: все в одном состоянии, считают вопросы.
 * @since 1.0
 */
final class FakeSessions implements Sessions {

    /**
     * Активны ли сессии.
     */
    private final boolean alive;

    /**
     * Сколько раз спросили.
     */
    private final AtomicInteger asked;

    /**
     * Конструктор.
     * @param alive Активны ли сессии
     */
    FakeSessions(final boolean alive) {
        this(alive, new AtomicInteger());
    }

    /**
     * Основной конструктор.
     * @param alive Активны ли сессии
     * @param asked Сколько раз спросили
     */
    FakeSessions(final boolean alive, final AtomicInteger asked) {
        this.alive = alive;
        this.asked = asked;
    }

    @Override
    public Mono<Boolean> active(final String sid) {
        return Mono.fromSupplier(
            () -> {
                this.asked.incrementAndGet();
                return this.alive;
            }
        );
    }

    /**
     * Сколько раз спросили.
     * @return Число вопросов
     */
    int asked() {
        return this.asked.get();
    }
}
