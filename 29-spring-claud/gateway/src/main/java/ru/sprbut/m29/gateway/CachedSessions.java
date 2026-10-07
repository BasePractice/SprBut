/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import reactor.core.publisher.Mono;

/**
 * Сессии с ответами, запомненными на короткое время.
 *
 * <p>Отзыв токена вступает в силу не позже чем через срок кэша, зато
 * gateway не ходит в auth на каждый запрос. Неудачные ответы не
 * запоминаются.</p>
 *
 * @since 1.0
 */
public final class CachedSessions implements Sessions {

    /**
     * Исходные сессии.
     */
    private final Sessions origin;

    /**
     * Запомненные ответы.
     */
    private final AsyncCache<String, Boolean> cache;

    /**
     * Конструктор.
     * @param origin Исходные сессии
     * @param ttl    Сколько помнить ответ
     */
    public CachedSessions(final Sessions origin, final Duration ttl) {
        this(origin, Caffeine.newBuilder().expireAfterWrite(ttl).<String, Boolean>buildAsync());
    }

    /**
     * Основной конструктор.
     * @param origin Исходные сессии
     * @param cache  Запомненные ответы
     */
    private CachedSessions(final Sessions origin, final AsyncCache<String, Boolean> cache) {
        this.origin = origin;
        this.cache = cache;
    }

    @Override
    public Mono<Boolean> active(final String sid) {
        return Mono.fromFuture(
            () -> this.cache.get(sid, (key, executor) -> this.origin.active(key).toFuture())
        );
    }
}
