/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m30;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Каналы уведомлений на pub/sub Redis.
 *
 * <p>Все подписки экземпляра идут через один контейнер, то есть через одно
 * соединение Redis: SUBSCRIBE и UNSUBSCRIBE на нём добавляют и убирают
 * каналы, не открывая новых соединений. Публикация идёт через шаблон,
 * потому что соединение в режиме подписки ничего, кроме подписок,
 * не принимает. Битое уведомление пропускается, не обрывая подписку.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor
@Slf4j
public final class RedisBus implements Bus {

    /**
     * Шаблон имени личного канала.
     */
    private static final String USER = "sse:user:%d";

    /**
     * Имя общего канала.
     */
    private static final String BROADCAST = "sse:broadcast";

    /**
     * Клиент Redis для публикации.
     */
    private final ReactiveStringRedisTemplate redis;

    /**
     * Контейнер подписок на одном соединении.
     */
    private final ReactiveRedisMessageListenerContainer container;

    /**
     * Разборщик JSON.
     */
    private final ObjectMapper mapper;

    @Override
    public Mono<Long> publish(final long user, final Notice notice) {
        return this.redis.convertAndSend(String.format(RedisBus.USER, user), notice.json());
    }

    @Override
    public Mono<Long> publish(final Notice notice) {
        return this.redis.convertAndSend(RedisBus.BROADCAST, notice.json());
    }

    @Override
    public Flux<ServerSentEvent<String>> user(final long user) {
        return this.events(String.format(RedisBus.USER, user));
    }

    @Override
    public Flux<ServerSentEvent<String>> broadcasts() {
        return this.events(RedisBus.BROADCAST);
    }

    private Flux<ServerSentEvent<String>> events(final String channel) {
        return this.container.receive(ChannelTopic.of(channel))
            .map(message -> new ParsedNotice(message.getMessage(), this.mapper))
            .concatMap(
                notice -> Mono.fromCallable(notice::sse)
                    .doOnError(
                        ex -> log.warn(
                            String.format(
                                "Уведомление %s из канала %s пропущено: его не удалось разобрать",
                                notice.json(), channel
                            ),
                            ex
                        )
                    )
                    .onErrorComplete()
            );
    }
}
