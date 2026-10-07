/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.gateway;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * Тесты {@link IdentityHeaders}.
 * @since 1.0
 */
final class IdentityHeadersTest {

    @Test
    @DisplayName("из анонимного запроса убирается поддельный заголовок пользователя")
    void dropsSpoofedUserFromAnonymousRequest() {
        final AtomicReference<HttpHeaders> passed = new AtomicReference<>();
        new IdentityHeaders().filter(
            MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/login").header("X-User-Id", "hacker")
            ),
            exchange -> {
                passed.set(exchange.getRequest().getHeaders());
                return Mono.empty();
            }
        ).block(Duration.ofSeconds(5));
        MatcherAssert.assertThat(
            "поддельный заголовок пользователя дошёл до сервиса",
            passed.get().getFirst("X-User-Id"),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("заголовок пользователя берётся из токена")
    void setsUserFromToken() {
        final String id = UUID.randomUUID().toString();
        final AtomicReference<HttpHeaders> passed = new AtomicReference<>();
        new IdentityHeaders().filter(
            MockServerWebExchange.from(
                MockServerHttpRequest.get("/profiles/x").header("X-User-Id", "hacker")
            ).mutate().principal(
                Mono.just(new JwtAuthenticationToken(new FakeJwt(id, "USER", "s").jwt()))
            ).build(),
            exchange -> {
                passed.set(exchange.getRequest().getHeaders());
                return Mono.empty();
            }
        ).block(Duration.ofSeconds(5));
        MatcherAssert.assertThat(
            "сервис не получил пользователя из токена",
            passed.get().getFirst("X-User-Id"),
            Matchers.equalTo(id)
        );
    }

    @Test
    @DisplayName("токен не уходит дальше gateway")
    void removesToken() {
        final AtomicReference<HttpHeaders> passed = new AtomicReference<>();
        new IdentityHeaders().filter(
            MockServerWebExchange.from(
                MockServerHttpRequest.get("/profiles/y").header("Authorization", "Bearer abc")
            ).mutate().principal(
                Mono.just(new JwtAuthenticationToken(new FakeJwt("y", "ADMIN", "s").jwt()))
            ).build(),
            exchange -> {
                passed.set(exchange.getRequest().getHeaders());
                return Mono.empty();
            }
        ).block(Duration.ofSeconds(5));
        MatcherAssert.assertThat(
            "токен дошёл до сервиса за gateway",
            passed.get().getFirst("Authorization"),
            Matchers.nullValue()
        );
    }
}
