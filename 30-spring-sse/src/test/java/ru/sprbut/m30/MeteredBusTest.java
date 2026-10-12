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
import org.springframework.http.codec.ServerSentEvent;

/**
 * Тесты {@link MeteredBus}.
 * @since 1.0
 */
final class MeteredBusTest {

    @Test
    @DisplayName("каналы считают опубликованные уведомления")
    void countsPublishedNotices() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Bus bus = new MeteredBus(new FakeBus(), registry);
        bus.publish(new TextNotice("broadcast", "раз")).block(Duration.ofSeconds(5L));
        bus.publish(3L, new TextNotice("user-event", "два")).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "каналы ошиблись в числе опубликованных уведомлений",
            registry.get("redis.sse.publish.total").counter().count(),
            Matchers.equalTo(2.0)
        );
    }

    @Test
    @DisplayName("каналы считают уведомления, полученные из личного и общего канала")
    void countsReceivedNotices() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Bus bus = new MeteredBus(new FakeBus(), registry);
        bus.publish(new TextNotice("broadcast", "всем")).block(Duration.ofSeconds(5L));
        bus.publish(4L, new TextNotice("user-event", "лично")).block(Duration.ofSeconds(5L));
        bus.broadcasts().take(1L).blockLast(Duration.ofSeconds(5L));
        bus.user(4L).take(1L).blockLast(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "каналы ошиблись в числе полученных уведомлений",
            registry.get("redis.sse.receive.total").counter().count(),
            Matchers.equalTo(2.0)
        );
    }

    @Test
    @DisplayName("каналы с метриками пропускают уведомления к исходным")
    void passesNoticesThrough() {
        final Bus bus = new MeteredBus(new FakeBus(), new SimpleMeterRegistry());
        final String text = UUID.randomUUID().toString();
        bus.publish(new TextNotice("broadcast", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "уведомление не прошло через исходные каналы",
            bus.broadcasts().next().map(ServerSentEvent::data).block(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }
}
