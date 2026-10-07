/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

/**
 * Связи сервиса profile с сервисами parameters и auth.
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
     * Обычный построитель HTTP-клиентов для всех, кроме сервиса параметров.
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
     * Характеристики пользователей в сервисе параметров.
     * @param builder Балансируемый построитель HTTP-клиентов
     * @param url     Адрес сервиса параметров, по умолчанию его имя в Eureka
     * @return Характеристики
     */
    @Bean
    static Parameters parameters(
        @LoadBalanced final RestClient.Builder builder,
        @Value("${parameters.url:http://parameters}") final String url
    ) {
        return new RemoteParameters(builder.clone().baseUrl(url).build());
    }

    /**
     * Учётки в сервисе auth.
     * @param builder Балансируемый построитель HTTP-клиентов
     * @param url     Адрес сервиса auth, по умолчанию его имя в Eureka
     * @return Учётки
     */
    @Bean
    static Accounts accounts(
        @LoadBalanced final RestClient.Builder builder,
        @Value("${auth.url:http://auth}") final String url
    ) {
        return new RemoteAccounts(builder.clone().baseUrl(url).build());
    }
}
