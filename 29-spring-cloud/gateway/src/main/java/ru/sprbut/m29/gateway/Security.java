/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Правила доступа gateway: регистрация, вход и обмен токена открыты,
 * всё остальное — с действительным access-токеном активной сессии.
 *
 * <p>Методы, которыми сервисы зовут друг друга, наружу закрыты: удаление
 * учётки идёт только через удаление профиля, а профиль заводится только
 * регистрацией. Иначе пользователь удалил бы учётку, оставив профиль
 * и характеристики без хозяина.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
public final class Security {

    /**
     * Конструктор для Spring.
     */
    private Security() {
    }

    /**
     * Цепочка фильтров безопасности.
     * @param http    Построитель правил
     * @param decoder Проверка access-токенов
     * @return Цепочка фильтров
     */
    @Bean
    static SecurityWebFilterChain chain(
        final ServerHttpSecurity http,
        final ReactiveJwtDecoder decoder
    ) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .authorizeExchange(
                rules -> rules
                    .pathMatchers(HttpMethod.DELETE, "/auth/users/*").denyAll()
                    .pathMatchers(HttpMethod.POST, "/profiles").denyAll()
                    .pathMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh")
                    .permitAll()
                    .anyExchange().authenticated()
            )
            .oauth2ResourceServer(server -> server.jwt(jwt -> jwt.jwtDecoder(decoder)))
            .build();
    }
}
