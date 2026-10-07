/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты {@link MeteredBus}.
 * @since 1.0
 */
final class MeteredBusTest {

    @Test
    @DisplayName("шина считает опубликованные уведомления")
    void countsPublishedNotices() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Bus bus = new MeteredBus(new FakeBus(), registry);
        bus.publish(new BroadcastNotice("раз")).block(Duration.ofSeconds(5L));
        bus.publish(new UserNotice(3L, "два")).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "шина ошиблась в числе опубликованных уведомлений",
            registry.get("redis.sse.publish.total").counter().count(),
            Matchers.equalTo(2.0)
        );
    }

    @Test
    @DisplayName("шина считает полученные уведомления")
    void countsReceivedNotices() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Bus bus = new MeteredBus(new FakeBus(), registry);
        bus.publish(new BroadcastNotice(UUID.randomUUID().toString()))
            .block(Duration.ofSeconds(5L));
        bus.notices().take(1L).blockLast(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "шина ошиблась в числе полученных уведомлений",
            registry.get("redis.sse.receive.total").counter().count(),
            Matchers.equalTo(1.0)
        );
    }

    @Test
    @DisplayName("шина с метриками пропускает уведомления к исходной")
    void passesNoticesThrough() {
        final Bus bus = new MeteredBus(new FakeBus(), new SimpleMeterRegistry());
        final String text = UUID.randomUUID().toString();
        bus.publish(new BroadcastNotice(text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "уведомление не прошло через исходную шину",
            bus.notices().next().map(Notice::json).block(Duration.ofSeconds(5L)),
            Matchers.containsString(text)
        );
    }
}
