/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Тесты {@link SseEndpoint}.
 * @since 1.0
 */
final class SseEndpointTest {

    @Test
    @DisplayName("молчащий поток получает пинги")
    void streamsPingsWhileSilent() {
        MatcherAssert.assertThat(
            "молчащий поток не получил пингов",
            new SseEndpoint(
                new SinkAudience(new ConcurrentHashMap<>(0), new FakeBus()), Duration.ofMillis(20L)
            ).stream(5L).map(ServerSentEvent::comment).take(2L).collectList()
                .block(Duration.ofSeconds(5L)),
            Matchers.contains("ping", "ping")
        );
    }

    @Test
    @DisplayName("поток передаёт событие из личного канала пользователя")
    void streamsUserChannelEvent() {
        final FakeBus bus = new FakeBus();
        final String text = UUID.randomUUID().toString();
        final Flux<String> data = new SseEndpoint(
            new SinkAudience(new ConcurrentHashMap<>(0), bus), Duration.ofSeconds(20L)
        ).stream(15L).map(ServerSentEvent::data).take(1L).cache();
        data.subscribe();
        bus.publish(15L, new TextNotice("user-event", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "событие личного канала не попало в поток",
            data.blockLast(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }
}
