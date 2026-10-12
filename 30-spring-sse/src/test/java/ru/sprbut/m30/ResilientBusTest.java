/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.time.Duration;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;

/**
 * Тесты {@link ResilientBus}.
 * @since 1.0
 */
final class ResilientBusTest {

    @Test
    @DisplayName("личный канал переподписывается после обрыва")
    void resubscribesUserChannelAfterBreak() {
        final FakeBus origin = new FakeBus(2);
        final String text = UUID.randomUUID().toString();
        origin.publish(8L, new TextNotice("user-event", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "личный канал не переподписался после обрыва",
            new ResilientBus(origin, Duration.ofMillis(10L)).user(8L)
                .map(ServerSentEvent::data).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }

    @Test
    @DisplayName("общий канал переподписывается после обрыва")
    void resubscribesBroadcastChannelAfterBreak() {
        final FakeBus origin = new FakeBus(2);
        final String text = UUID.randomUUID().toString();
        origin.publish(new TextNotice("broadcast", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "общий канал не переподписался после обрыва",
            new ResilientBus(origin, Duration.ofMillis(10L)).broadcasts()
                .map(ServerSentEvent::data).blockFirst(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }

    @Test
    @DisplayName("отменённая подписка на личный канал не восстанавливается")
    void doesntResubscribeAfterCancel() {
        final FakeBus origin = new FakeBus();
        new ResilientBus(origin, Duration.ofMillis(10L)).user(9L).subscribe().dispose();
        MatcherAssert.assertThat(
            "отменённая подписка на личный канал восстановилась",
            origin.listeners(9L),
            Matchers.equalTo(0)
        );
    }
}
