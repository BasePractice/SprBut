/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import ru.sprbut.m29.identity.HeaderAuthentication;

/**
 * Правила доступа сервиса auth.
 *
 * <p>Пользователя приносит gateway в заголовках X-User-*. Регистрация,
 * вход, обмен и методы для gateway открыты; смена роли — только для
 * {@link Role#ADMIN}; остальное по учётке — самому пользователю и
 * администратору.</p>
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public final class Security {

    /**
     * Конструктор для Spring.
     */
    private Security() {
    }

    /**
     * Цепочка фильтров безопасности.
     * @param http Построитель правил
     * @return Цепочка фильтров
     * @throws Exception Если правила не собрались
     */
    @Bean
    static SecurityFilterChain chain(final HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(new HeaderAuthentication(), AuthorizationFilter.class)
            .exceptionHandling(
                handling -> handling.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            )
            .authorizeHttpRequests(
                rules -> rules
                    .requestMatchers(
                        HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh"
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/internal/**", "/.well-known/jwks.json")
                    .permitAll()
                    .requestMatchers("/auth/users/{id}/role").hasRole(Role.ADMIN.name())
                    .requestMatchers("/auth/users/{id}", "/auth/users/{id}/**").access(
                        new WebExpressionAuthorizationManager(
                            "#id == authentication.name or hasRole('ADMIN')"
                        )
                    )
                    .anyRequest().authenticated()
            )
            .build();
    }

    /**
     * Хеширование паролей.
     * @return BCrypt
     */
    @Bean
    static PasswordEncoder passwords() {
        return new BCryptPasswordEncoder();
    }
}
