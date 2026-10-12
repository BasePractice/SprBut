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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

/**
 * Сборка сервиса: пользователи, каналы Redis, подписка на общий канал
 * и пулы потоков, всё с метриками.
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Pools.class)
public final class SseConfiguration {

    /**
     * Конструктор для Spring.
     */
    private SseConfiguration() {
    }

    /**
     * Контейнер подписок экземпляра: все SUBSCRIBE идут через одно соединение.
     * @param factory Фабрика соединений Redis
     * @return Контейнер, закрываемый вместе с контекстом
     */
    @Bean
    static ReactiveRedisMessageListenerContainer container(
        final ReactiveRedisConnectionFactory factory
    ) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }

    /**
     * Пул доставки событий из каналов Redis в потоки пользователей.
     * @param pools Настройки пулов
     * @param registry Реестр метрик
     * @return Планировщик, закрываемый вместе с контекстом
     */
    @Bean(destroyMethod = "dispose")
    static Scheduler delivery(final Pools pools, final MeterRegistry registry) {
        return Schedulers.fromExecutorService(
            pools.delivery().executor("sse.delivery", registry), "sse.delivery"
        );
    }

    /**
     * Пул, в котором сервер gRPC выполняет вызовы.
     * @param pools Настройки пулов
     * @param registry Реестр метрик
     * @return Пул, закрываемый вместе с контекстом
     */
    @Bean
    static GrpcExecutor commands(final Pools pools, final MeterRegistry registry) {
        return new GrpcExecutor(pools.commands().executor("grpc.commands", registry));
    }

    /**
     * Каналы уведомлений.
     * @param redis Клиент Redis
     * @param container Контейнер подписок
     * @param mapper Разборщик JSON
     * @param delivery Пул доставки
     * @param registry Реестр метрик
     * @param pause Пауза перед первой переподпиской
     * @return Каналы
     * @checkstyle ParameterNumberCheck (8 lines)
     */
    @Bean
    static Bus bus(
        final ReactiveStringRedisTemplate redis,
        final ReactiveRedisMessageListenerContainer container,
        final ObjectMapper mapper,
        final Scheduler delivery,
        final MeterRegistry registry,
        @Value("${sse.retry-pause:1s}") final Duration pause
    ) {
        return new MeteredBus(
            new ResilientBus(
                new ScheduledBus(new RedisBus(redis, container, mapper), delivery), pause
            ),
            registry
        );
    }

    /**
     * Подключённые к экземпляру пользователи.
     * @param bus Каналы уведомлений
     * @param registry Реестр метрик
     * @return Пользователи
     */
    @Bean
    static Audience audience(final Bus bus, final MeterRegistry registry) {
        return new MeteredAudience(new SinkAudience(new ConcurrentHashMap<>(0), bus), registry);
    }

    /**
     * Подписка экземпляра на общий канал.
     * @param bus Каналы уведомлений
     * @param audience Пользователи
     * @return Подписка, запускаемая вместе с контекстом
     */
    @Bean
    static Relay relay(final Bus bus, final Audience audience) {
        return new Relay(bus, audience);
    }
}
