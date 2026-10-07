/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.web.client.RestClient;

/**
 * Сессии в базе и связь с сервисом profile.
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class Wiring {

    /**
     * Конструктор для Spring.
     */
    private Wiring() {
    }

    /**
     * Сессии в PostgreSQL.
     * @param jdbc Клиент базы
     * @param txn  Транзакции
     * @param ttl  Срок жизни refresh-токена
     * @return Сессии
     */
    @Bean
    static Sessions sessions(
        final JdbcClient jdbc,
        final TransactionOperations txn,
        @Value("${jwt.refresh-ttl:P30D}") final Duration ttl
    ) {
        return new PgSessions(jdbc, txn, Clock.systemUTC(), ttl);
    }

    /**
     * Обычный построитель HTTP-клиентов для всех, кроме вызовов по имени сервиса.
     *
     * <p>Без него балансируемый построитель достаётся и самому клиенту Eureka,
     * и тот ищет адрес реестра в реестре.</p>
     *
     * @return Построитель
     */
    @Bean
    @Primary
    static RestClient.Builder plain() {
        return RestClient.builder();
    }

    /**
     * Построитель HTTP-клиентов, разрешающий имена сервисов через Eureka.
     * @return Построитель
     */
    @Bean
    @LoadBalanced
    static RestClient.Builder balanced() {
        return RestClient.builder();
    }

    /**
     * Профили в сервисе profile.
     * @param builder Балансируемый построитель HTTP-клиентов
     * @param url     Адрес сервиса profile, по умолчанию его имя в Eureka
     * @return Профили
     */
    @Bean
    static Profiles profiles(
        @LoadBalanced final RestClient.Builder builder,
        @Value("${profile.url:http://profile}") final String url
    ) {
        return new RemoteProfiles(builder.clone().baseUrl(url).build());
    }
}
