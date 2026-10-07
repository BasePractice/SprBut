/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Пользователи этого экземпляра, у каждого — своя толпа открытых потоков.
 *
 * <p>Вход и выход подписчика меняют карту только атомарно, внутри {@link Map#compute},
 * поэтому толпа исчезает ровно тогда, когда уходит её последний подписчик.</p>
 *
 * @since 1.0
 */
public final class SinkAudience implements Audience {

    /**
     * Толпы по пользователям.
     */
    private final ConcurrentMap<Long, Crowd> map;

    /**
     * Конструктор.
     * @param map Толпы по пользователям
     */
    SinkAudience(final ConcurrentMap<Long, Crowd> map) {
        this.map = map;
    }

    @Override
    public Flux<ServerSentEvent<String>> stream(final long user) {
        return Flux.defer(
            () -> this.map.compute(user, (key, crowd) -> SinkAudience.joined(crowd)).flux()
        ).doFinally(
            signal -> this.map.computeIfPresent(user, (key, crowd) -> SinkAudience.left(crowd))
        );
    }

    @Override
    public void deliver(final long user, final ServerSentEvent<String> event) {
        Optional.ofNullable(this.map.get(user)).ifPresent(crowd -> crowd.emit(event));
    }

    @Override
    public void broadcast(final ServerSentEvent<String> event) {
        this.map.values().forEach(crowd -> crowd.emit(event));
    }

    private static Crowd joined(final Crowd crowd) {
        return Optional.ofNullable(crowd).map(Crowd::joined).orElseGet(Crowd::new);
    }

    private static Crowd left(final Crowd crowd) {
        final Crowd rest;
        if (crowd.last()) {
            rest = null;
        } else {
            rest = crowd.left();
        }
        return rest;
    }
}
