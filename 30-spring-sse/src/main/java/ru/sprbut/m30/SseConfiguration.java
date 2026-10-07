/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Сборка сервиса: пользователи, канал Redis и подписка на него, всё с метриками.
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class SseConfiguration {

    /**
     * Конструктор для Spring.
     */
    private SseConfiguration() {
    }

    /**
     * Подключённые к экземпляру пользователи.
     * @param registry Реестр метрик
     * @return Пользователи
     */
    @Bean
    static Audience audience(final MeterRegistry registry) {
        return new MeteredAudience(new SinkAudience(new ConcurrentHashMap<>(0)), registry);
    }

    /**
     * Общий канал уведомлений.
     * @param redis Клиент Redis
     * @param mapper Разборщик JSON
     * @param registry Реестр метрик
     * @param topic Имя канала
     * @return Канал
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    @Bean
    static Bus bus(
        final ReactiveStringRedisTemplate redis,
        final ObjectMapper mapper,
        final MeterRegistry registry,
        @Value("${sse.topic:sse-events}") final String topic
    ) {
        return new MeteredBus(new RedisBus(redis, topic, mapper), registry);
    }

    /**
     * Подписка экземпляра на общий канал.
     * @param bus Канал
     * @param audience Пользователи
     * @param pause Пауза перед первой переподпиской
     * @return Подписка, запускаемая вместе с контекстом
     */
    @Bean
    static Relay relay(
        final Bus bus,
        final Audience audience,
        @Value("${sse.retry-pause:1s}") final Duration pause
    ) {
        return new Relay(bus, audience, pause);
    }
}
