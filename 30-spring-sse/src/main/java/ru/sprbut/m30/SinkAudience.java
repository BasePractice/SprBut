/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m30;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Пользователи этого экземпляра, у каждого — своя толпа открытых потоков.
 *
 * <p>Первый поток пользователя подписывает экземпляр на личный канал
 * пользователя, последний — отписывает. Вход и выход подписчика меняют карту
 * только атомарно, внутри {@link Map#compute}, поэтому толпа и её подписка
 * исчезают ровно тогда, когда уходит последний подписчик.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SinkAudience implements Audience {

    /**
     * Толпы по пользователям.
     */
    private final ConcurrentMap<Long, Crowd> map;

    /**
     * Каналы уведомлений.
     */
    private final Bus bus;

    @Override
    public Flux<ServerSentEvent<String>> stream(final long user) {
        return Flux.defer(
            () -> this.map.compute(user, this::joined).flux()
        ).doFinally(
            signal -> this.map.computeIfPresent(user, (key, crowd) -> SinkAudience.left(crowd))
        );
    }

    @Override
    public void broadcast(final ServerSentEvent<String> event) {
        this.map.values().forEach(crowd -> crowd.emit(event));
    }

    private Crowd joined(final long user, final Crowd crowd) {
        return Optional.ofNullable(crowd).map(Crowd::joined).orElseGet(
            () -> new Crowd().attached(this.bus.user(user))
        );
    }

    private static Crowd left(final Crowd crowd) {
        final Crowd rest;
        if (crowd.last()) {
            crowd.close();
            rest = null;
        } else {
            rest = crowd.left();
        }
        return rest;
    }
}
