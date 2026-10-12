/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Каналы, которые не принимают публикаций, как отключённый Redis.
 * @since 1.0
 */
final class BrokenBus implements Bus {

    @Override
    public Mono<Long> publish(final long user, final Notice notice) {
        return Mono.error(new IllegalStateException("Redis отключён намеренно"));
    }

    @Override
    public Mono<Long> publish(final Notice notice) {
        return Mono.error(new IllegalStateException("Redis отключён намеренно"));
    }

    @Override
    public Flux<ServerSentEvent<String>> user(final long user) {
        return Flux.never();
    }

    @Override
    public Flux<ServerSentEvent<String>> broadcasts() {
        return Flux.never();
    }
}
