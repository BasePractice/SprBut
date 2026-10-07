/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.gateway;

import java.time.Duration;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * Тесты {@link ActiveTokens}.
 * @since 1.0
 */
final class ActiveTokensTest {

    @Test
    @DisplayName("токен активной сессии принимается")
    void passesTokenOfActiveSession() {
        final Jwt jwt = new FakeJwt(UUID.randomUUID().toString(), "USER", "s-5").jwt();
        MatcherAssert.assertThat(
            "токен активной сессии отклонён",
            new ActiveTokens(token -> Mono.just(jwt), new FakeSessions(true))
                .decode("ignored").block(Duration.ofSeconds(5)),
            Matchers.equalTo(jwt)
        );
    }

    @Test
    @DisplayName("токен закрытой сессии отклоняется")
    void rejectsTokenOfClosedSession() {
        Assertions.assertThrows(
            BadJwtException.class,
            () -> new ActiveTokens(
                token -> Mono.just(new FakeJwt("u", "USER", "s-6").jwt()),
                new FakeSessions(false)
            ).decode("ignored").block(Duration.ofSeconds(5))
        );
    }
}
