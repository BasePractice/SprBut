/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Каналы в памяти, которые считают подписки и могут обрывать первые из них.
 *
 * <p>В отличие от Redis, каналы помнят всё опубликованное: тесту не нужно
 * ждать, пока подписка дойдёт до канала.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class FakeBus implements Bus {

    /**
     * Опубликованные события с именами каналов.
     */
    private final Sinks.Many<Map.Entry<String, ServerSentEvent<String>>> sink;

    /**
     * Число подписок по каналам.
     */
    private final ConcurrentMap<String, AtomicInteger> map;

    /**
     * Сколько подписок ещё оборвать.
     */
    private final AtomicInteger breaks;

    /**
     * Конструктор надёжных каналов.
     */
    FakeBus() {
        this(0);
    }

    /**
     * Конструктор каналов, обрывающих первые подписки.
     * @param breaks Сколько подписок оборвать
     */
    FakeBus(final int breaks) {
        this(Sinks.many().replay().all(), new ConcurrentHashMap<>(0), new AtomicInteger(breaks));
    }

    @Override
    public Mono<Long> publish(final long user, final Notice notice) {
        return this.send(String.format("user:%d", user), notice);
    }

    @Override
    public Mono<Long> publish(final Notice notice) {
        return this.send("broadcast", notice);
    }

    @Override
    public Flux<ServerSentEvent<String>> user(final long user) {
        return this.channel(String.format("user:%d", user));
    }

    @Override
    public Flux<ServerSentEvent<String>> broadcasts() {
        return this.channel("broadcast");
    }

    /**
     * Сколько подписок на личный канал пользователя открыто сейчас.
     * @param user Идентификатор пользователя
     * @return Число подписок
     */
    int listeners(final long user) {
        return this.counter(String.format("user:%d", user)).get();
    }

    private Mono<Long> send(final String channel, final Notice notice) {
        return Mono.fromCallable(
            () -> {
                this.sink.emitNext(
                    Map.entry(channel, notice.sse()),
                    Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1L))
                );
                return (long) this.counter(channel).get();
            }
        );
    }

    private Flux<ServerSentEvent<String>> channel(final String channel) {
        return Flux.defer(
            () -> {
                final Flux<ServerSentEvent<String>> flux;
                if (this.breaks.getAndDecrement() > 0) {
                    flux = Flux.error(
                        new UncheckedIOException(new IOException("Канал оборван намеренно"))
                    );
                } else {
                    flux = this.sink.asFlux()
                        .filter(entry -> channel.equals(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .doOnSubscribe(sub -> this.counter(channel).incrementAndGet())
                        .doFinally(signal -> this.counter(channel).decrementAndGet());
                }
                return flux;
            }
        );
    }

    private AtomicInteger counter(final String channel) {
        return this.map.computeIfAbsent(channel, key -> new AtomicInteger(0));
    }
}
