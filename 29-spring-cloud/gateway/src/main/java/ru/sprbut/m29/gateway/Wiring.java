/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Связи gateway с сервисами auth, profile и parameters через Eureka.
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
     * Обычный построитель HTTP-клиентов для всех, кроме вызовов по имени сервиса.
     * @return Построитель
     */
    @Bean
    @Primary
    static WebClient.Builder plain() {
        return WebClient.builder();
    }

    /**
     * Построитель HTTP-клиентов, разрешающий имена сервисов через Eureka.
     * @return Построитель
     */
    @Bean
    @LoadBalanced
    static WebClient.Builder balanced() {
        return WebClient.builder();
    }

    /**
     * Сессии в auth с ответами, запомненными на короткое время.
     * @param builder Балансируемый построитель HTTP-клиентов
     * @param url     Адрес сервиса auth
     * @param ttl     Сколько помнить ответ о сессии
     * @return Сессии
     */
    @Bean
    static Sessions sessions(
        @LoadBalanced final WebClient.Builder builder,
        @Value("${auth.url:http://auth}") final String url,
        @Value("${gateway.session-cache:PT10S}") final Duration ttl
    ) {
        return new CachedSessions(new RemoteSessions(builder.clone().baseUrl(url).build()), ttl);
    }

    /**
     * Проверка access-токенов: подпись по JWKS из auth и активность сессии.
     * @param builder  Балансируемый построитель HTTP-клиентов
     * @param sessions Сессии
     * @param url      Адрес сервиса auth
     * @return Проверка
     */
    @Bean
    static ReactiveJwtDecoder decoder(
        @LoadBalanced final WebClient.Builder builder,
        final Sessions sessions,
        @Value("${auth.url:http://auth}") final String url
    ) {
        return new ActiveTokens(
            NimbusReactiveJwtDecoder.withJwkSetUri(String.format("%s/.well-known/jwks.json", url))
                .webClient(builder.clone().build())
                .build(),
            sessions
        );
    }

    /**
     * Профили в сервисе profile.
     * @param builder Балансируемый построитель HTTP-клиентов
     * @param url     Адрес сервиса profile
     * @return Профили
     */
    @Bean
    static Profiles profiles(
        @LoadBalanced final WebClient.Builder builder,
        @Value("${profile.url:http://profile}") final String url
    ) {
        return new RemoteProfiles(builder.clone().baseUrl(url).build());
    }

    /**
     * Характеристики в сервисе parameters.
     * @param builder Балансируемый построитель HTTP-клиентов
     * @param url     Адрес сервиса parameters
     * @return Характеристики
     */
    @Bean
    static Parameters parameters(
        @LoadBalanced final WebClient.Builder builder,
        @Value("${parameters.url:http://parameters}") final String url
    ) {
        return new RemoteParameters(builder.clone().baseUrl(url).build());
    }

    /**
     * Замена заголовков пользователя на маршрутах.
     * @return Фильтр
     */
    @Bean
    static IdentityHeaders headers() {
        return new IdentityHeaders();
    }
}
