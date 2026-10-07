/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

/**
 * Канал уведомлений на pub/sub Redis.
 * @since 1.0
 */
public final class RedisBus implements Bus {

    /**
     * Клиент Redis.
     */
    private final ReactiveStringRedisTemplate redis;

    /**
     * Имя канала.
     */
    private final String topic;

    /**
     * Разборщик JSON.
     */
    private final ObjectMapper mapper;

    /**
     * Конструктор.
     * @param redis Клиент Redis
     * @param topic Имя канала
     * @param mapper Разборщик JSON
     */
    public RedisBus(
        final ReactiveStringRedisTemplate redis,
        final String topic,
        final ObjectMapper mapper
    ) {
        this.redis = redis;
        this.topic = topic;
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> publish(final Notice notice) {
        return this.redis.convertAndSend(this.topic, notice.json()).then();
    }

    @Override
    public Flux<Notice> notices() {
        return this.redis.listenToChannel(this.topic).map(
            message -> new ParsedNotice(message.getMessage(), this.mapper)
        );
    }
}
