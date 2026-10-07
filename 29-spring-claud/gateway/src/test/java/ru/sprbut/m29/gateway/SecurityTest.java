/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.gateway;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * Правила доступа {@link Security}.
 * @since 1.0
 */
final class SecurityTest {

    @Test
    @DisplayName("снаружи нельзя удалить учётку в обход профиля")
    void dontLetUserDeleteAccountPastProfile() {
        final String id = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "gateway пропустил внутреннее удаление учётки, и профиль остался без хозяина",
            WebTestClient.bindToWebHandler(
                exchange -> exchange.getResponse().setComplete()
            ).webFilter(
                new WebFilterChainProxy(
                    Security.chain(
                        ServerHttpSecurity.http(),
                        token -> Mono.just(new FakeJwt(id, "USER", "s").jwt())
                    )
                )
            )
            .build()
            .delete()
            .uri("/auth/users/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
            .exchange()
            .returnResult(Void.class)
            .getStatus()
            .value(),
            Matchers.equalTo(403)
        );
    }

    @Test
    @DisplayName("снаружи нельзя завести профиль в обход регистрации")
    void dontLetUserCreateProfilePastRegistration() {
        final String id = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "gateway пропустил внутреннее создание профиля",
            WebTestClient.bindToWebHandler(
                exchange -> exchange.getResponse().setComplete()
            ).webFilter(
                new WebFilterChainProxy(
                    Security.chain(
                        ServerHttpSecurity.http(),
                        token -> Mono.just(new FakeJwt(id, "ADMIN", "s").jwt())
                    )
                )
            )
            .build()
            .post()
            .uri("/profiles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
            .exchange()
            .returnResult(Void.class)
            .getStatus()
            .value(),
            Matchers.equalTo(403)
        );
    }

    @Test
    @DisplayName("удаление пользователя через профиль gateway пропускает")
    void letsUserDeleteOwnProfile() {
        final String id = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "gateway не пропустил удаление пользователя через профиль",
            WebTestClient.bindToWebHandler(
                exchange -> exchange.getResponse().setComplete()
            ).webFilter(
                new WebFilterChainProxy(
                    Security.chain(
                        ServerHttpSecurity.http(),
                        token -> Mono.just(new FakeJwt(id, "USER", "s").jwt())
                    )
                )
            )
            .build()
            .delete()
            .uri("/profiles/{id}", id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
            .exchange()
            .returnResult(Void.class)
            .getStatus()
            .value(),
            Matchers.equalTo(200)
        );
    }
}
