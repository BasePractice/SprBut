/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.gateway;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Тесты {@link MeEndpoint}.
 * @since 1.0
 */
final class MeEndpointTest {

    @Test
    @DisplayName("в профиль из /me добавляется роль из токена")
    void addsRoleFromTokenToProfile() {
        MatcherAssert.assertThat(
            "профиль в /me не получил роль из токена",
            new MeEndpoint(
                owner -> Mono.just(Map.of("name", "Ада")),
                owner -> Mono.just(Map.of())
            ).current(new FakeJwt("ada", "ADMIN", "s").jwt())
                .block(Duration.ofSeconds(5)).get("profile"),
            Matchers.equalTo(Map.of("name", "Ада", "role", "ADMIN"))
        );
    }

    @Test
    @DisplayName("характеристики запрашиваются для владельца токена")
    void asksParametersOfTokenOwner() {
        final String id = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "характеристики в /me принадлежат не владельцу токена",
            new MeEndpoint(
                owner -> Mono.just(Map.of()),
                owner -> Mono.just(Map.of("owner", owner))
            ).current(new FakeJwt(id, "USER", "s").jwt())
                .block(Duration.ofSeconds(5)).get("parameters"),
            Matchers.equalTo(Map.of("owner", id))
        );
    }

    @Test
    @DisplayName("профиль и характеристики запрашиваются параллельно")
    void asksProfileAndParametersInParallel() {
        final long start = System.nanoTime();
        new MeEndpoint(
            owner -> Mono.just(Map.<String, Object>of()).delayElement(Duration.ofSeconds(1)),
            owner -> Mono.just(Map.<String, String>of()).delayElement(Duration.ofSeconds(1))
        ).current(new FakeJwt("p", "USER", "s").jwt()).block(Duration.ofSeconds(5));
        MatcherAssert.assertThat(
            "профиль и характеристики запрошены друг за другом, а не параллельно",
            Duration.ofNanos(System.nanoTime() - start),
            Matchers.lessThan(Duration.ofMillis(1500))
        );
    }

    @Test
    @DisplayName("сбой характеристик превращается в ответ 502")
    void answersBadGatewayWhenParametersFail() {
        MatcherAssert.assertThat(
            "сбой характеристик не превратился в 502 Bad Gateway",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new MeEndpoint(
                    owner -> Mono.just(Map.of()),
                    owner -> Mono.error(
                        new WebClientRequestException(
                            new IOException("сервис характеристик недоступен"),
                            HttpMethod.GET,
                            URI.create("http://parameters"),
                            new HttpHeaders()
                        )
                    )
                ).current(new FakeJwt("q", "USER", "s").jwt()).block(Duration.ofSeconds(5))
            ).getStatusCode().value(),
            Matchers.equalTo(502)
        );
    }
}
