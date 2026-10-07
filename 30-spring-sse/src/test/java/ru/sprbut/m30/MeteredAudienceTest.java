/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Mono;

/**
 * Тесты {@link MeteredAudience}.
 * @since 1.0
 */
final class MeteredAudienceTest {

    @Test
    @DisplayName("аудитория считает активные потоки")
    void countsActiveStreams() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Audience audience = new MeteredAudience(
            new SinkAudience(new ConcurrentHashMap<>(0)), registry
        );
        audience.stream(1L).subscribe();
        audience.stream(2L).subscribe();
        audience.stream(3L).subscribe().dispose();
        MatcherAssert.assertThat(
            "аудитория ошиблась в числе активных потоков",
            registry.get("sse.server.active.connections").gauge().value(),
            Matchers.equalTo(2.0)
        );
    }

    @Test
    @DisplayName("аудитория считает открытые за всё время потоки")
    void countsOpenedStreams() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Audience audience = new MeteredAudience(
            new SinkAudience(new ConcurrentHashMap<>(0)), registry
        );
        audience.stream(4L).subscribe().dispose();
        audience.stream(4L).subscribe().dispose();
        MatcherAssert.assertThat(
            "аудитория ошиблась в числе открытых потоков",
            registry.get("sse.server.connections.opened").counter().count(),
            Matchers.equalTo(2.0)
        );
    }

    @Test
    @DisplayName("аудитория считает отправленные события")
    void countsSentEvents() {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final Audience audience = new MeteredAudience(
            new SinkAudience(new ConcurrentHashMap<>(0)), registry
        );
        audience.stream(5L).take(2L).subscribe();
        audience.deliver(5L, ServerSentEvent.builder("раз").build());
        audience.broadcast(ServerSentEvent.builder("два").build());
        MatcherAssert.assertThat(
            "аудитория ошиблась в числе отправленных событий",
            registry.get("sse.server.events.sent").counter().count(),
            Matchers.equalTo(2.0)
        );
    }

    @Test
    @DisplayName("аудитория с метриками пропускает события к исходной")
    void passesEventsThrough() {
        final Audience audience = new MeteredAudience(
            new SinkAudience(new ConcurrentHashMap<>(0)), new SimpleMeterRegistry()
        );
        final Mono<String> first = audience.stream(6L)
            .map(ServerSentEvent::data).next().cache();
        first.subscribe();
        audience.deliver(6L, ServerSentEvent.builder("насквозь").build());
        MatcherAssert.assertThat(
            "событие не дошло до исходной аудитории",
            first.block(Duration.ofSeconds(5L)),
            Matchers.equalTo("насквозь")
        );
    }
}
