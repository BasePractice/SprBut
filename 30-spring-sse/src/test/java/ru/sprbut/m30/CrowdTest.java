/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;

/**
 * Тесты {@link Crowd}.
 * @since 1.0
 */
final class CrowdTest {

    @Test
    @DisplayName("новая толпа состоит из одного подписчика")
    void startsWithSingleSubscriber() {
        MatcherAssert.assertThat(
            "новая толпа состоит не из одного подписчика",
            new Crowd().last(),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("толпа растёт, когда присоединяется подписчик")
    void growsWhenSubscriberJoins() {
        MatcherAssert.assertThat(
            "толпа осталась из одного подписчика после присоединения второго",
            new Crowd().joined().last(),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("толпа сжимается, когда подписчики уходят")
    void shrinksWhenSubscriberLeaves() {
        MatcherAssert.assertThat(
            "толпа не вернулась к одному подписчику после ухода остальных",
            new Crowd().joined().joined().left().left().last(),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("после присоединения толпа делит прежний источник событий")
    void sharesSourceAfterJoin() throws Exception {
        final Crowd crowd = new Crowd();
        final String text = UUID.randomUUID().toString();
        final Future<List<String>> received = crowd.joined().flux()
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        crowd.emit(ServerSentEvent.builder(text).build());
        MatcherAssert.assertThat(
            "толпа после присоединения потеряла общий источник событий",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }
}
