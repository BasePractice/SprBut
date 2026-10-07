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
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Тесты {@link SseEndpoint}.
 * @since 1.0
 */
final class SseEndpointTest {

    @Test
    @DisplayName("точка отправки публикует личное уведомление")
    void publishesUserNotice() {
        final FakeBus bus = new FakeBus();
        final String text = UUID.randomUUID().toString();
        WebTestClient.bindToController(
            new SseEndpoint(new FakeAudience(), bus, Duration.ofSeconds(20L))
        ).build().post().uri("/sse/send/user/{user}", 77L)
            .contentType(MediaType.TEXT_PLAIN).bodyValue(text)
            .exchange().expectStatus().isOk();
        MatcherAssert.assertThat(
            "личное уведомление не опубликовано",
            bus.notices().next().map(Notice::json).block(Duration.ofSeconds(5L)),
            Matchers.allOf(Matchers.containsString("\"userId\":77"), Matchers.containsString(text))
        );
    }

    @Test
    @DisplayName("точка отправки публикует общее уведомление")
    void publishesBroadcastNotice() {
        final FakeBus bus = new FakeBus();
        final String text = UUID.randomUUID().toString();
        WebTestClient.bindToController(
            new SseEndpoint(new FakeAudience(), bus, Duration.ofSeconds(20L))
        ).build().post().uri("/sse/send/broadcast")
            .contentType(MediaType.TEXT_PLAIN).bodyValue(text)
            .exchange().expectStatus().isOk();
        MatcherAssert.assertThat(
            "общее уведомление не опубликовано",
            bus.notices().next().map(Notice::json).block(Duration.ofSeconds(5L)),
            Matchers.allOf(Matchers.containsString("BROADCAST"), Matchers.containsString(text))
        );
    }

    @Test
    @DisplayName("нечисловой пользователь отвергается кодом 400")
    void rejectsNonNumericUser() {
        MatcherAssert.assertThat(
            "нечисловой пользователь не отвергнут кодом 400",
            WebTestClient.bindToController(
                new SseEndpoint(new FakeAudience(), new FakeBus(), Duration.ofSeconds(20L))
            ).build().post().uri("/sse/send/user/{user}", "ничей")
                .contentType(MediaType.TEXT_PLAIN).bodyValue("текст")
                .exchange().returnResult(String.class).getStatus().value(),
            Matchers.equalTo(400)
        );
    }

    @Test
    @DisplayName("молчащий поток получает пинги")
    void streamsPingsWhileSilent() {
        MatcherAssert.assertThat(
            "молчащий поток не получил пингов",
            new SseEndpoint(
                new SinkAudience(new ConcurrentHashMap<>(0)), new FakeBus(), Duration.ofMillis(20L)
            ).stream(5L).map(ServerSentEvent::comment).take(2L).collectList()
                .block(Duration.ofSeconds(5L)),
            Matchers.contains("ping", "ping")
        );
    }

    @Test
    @DisplayName("поток передаёт доставленное событие")
    void streamsDeliveredEvent() {
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0));
        final String text = UUID.randomUUID().toString();
        final Flux<String> data = new SseEndpoint(audience, new FakeBus(), Duration.ofSeconds(20L))
            .stream(15L).map(ServerSentEvent::data).take(1L).cache();
        data.subscribe();
        audience.deliver(15L, ServerSentEvent.builder(text).event("user-event").build());
        MatcherAssert.assertThat(
            "доставленное событие не попало в поток",
            data.blockLast(Duration.ofSeconds(5L)),
            Matchers.equalTo(text)
        );
    }
}
