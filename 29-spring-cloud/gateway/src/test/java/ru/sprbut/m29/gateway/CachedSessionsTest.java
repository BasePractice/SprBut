/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.gateway;

import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты {@link CachedSessions}.
 * @since 1.0
 */
final class CachedSessionsTest {

    @Test
    @DisplayName("в пределах ttl кеш спрашивает auth один раз")
    void asksOriginOnceWithinTtl() {
        final FakeSessions origin = new FakeSessions(true);
        final Sessions cached = new CachedSessions(origin, Duration.ofMinutes(1));
        cached.active("s-1").block(Duration.ofSeconds(5));
        cached.active("s-1").block(Duration.ofSeconds(5));
        MatcherAssert.assertThat(
            "кеш дважды спросил auth об одной и той же сессии",
            origin.asked(),
            Matchers.equalTo(1)
        );
    }

    @Test
    @DisplayName("о другой сессии кеш спрашивает auth заново")
    void asksAgainAboutAnotherSession() {
        final FakeSessions origin = new FakeSessions(false);
        final Sessions cached = new CachedSessions(origin, Duration.ofMinutes(1));
        cached.active("s-2").block(Duration.ofSeconds(5));
        cached.active("s-3").block(Duration.ofSeconds(5));
        MatcherAssert.assertThat(
            "кеш перепутал разные сессии",
            origin.asked(),
            Matchers.equalTo(2)
        );
    }

    @Test
    @DisplayName("кеш возвращает ответ auth без изменений")
    void keepsAnswerOfOrigin() {
        MatcherAssert.assertThat(
            "кеш изменил ответ auth",
            new CachedSessions(new FakeSessions(false), Duration.ofSeconds(9))
                .active("s-4").block(Duration.ofSeconds(5)),
            Matchers.is(false)
        );
    }
}
