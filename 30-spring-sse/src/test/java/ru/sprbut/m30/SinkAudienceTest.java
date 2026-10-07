/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Тесты {@link SinkAudience}.
 * @since 1.0
 */
final class SinkAudienceTest {

    @Test
    @DisplayName("событие доходит до подключённого пользователя")
    void deliversEventToConnectedUser() throws Exception {
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0));
        final long user = ThreadLocalRandom.current().nextLong(1L, 1000L);
        final String text = UUID.randomUUID().toString();
        final Future<List<String>> received = audience.stream(user)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        audience.deliver(user, ServerSentEvent.builder(text).build());
        MatcherAssert.assertThat(
            "событие не дошло до подключённого пользователя",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("событие другого пользователя не попадает в чужой поток")
    void doesntDeliverEventOfAnotherUser() throws Exception {
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0));
        final String text = UUID.randomUUID().toString();
        final Future<List<String>> received = audience.stream(11L)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        audience.stream(12L).subscribe();
        audience.deliver(12L, ServerSentEvent.builder("чужое").build());
        audience.deliver(11L, ServerSentEvent.builder(text).build());
        MatcherAssert.assertThat(
            "в поток попало событие другого пользователя",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("событие доходит до каждого потока одного пользователя")
    void deliversEventToEveryStreamOfUser() throws Exception {
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0));
        final String text = UUID.randomUUID().toString();
        audience.stream(21L).subscribe();
        final Future<List<String>> received = audience.stream(21L)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        audience.deliver(21L, ServerSentEvent.builder(text).build());
        MatcherAssert.assertThat(
            "второй поток того же пользователя не получил событие",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("общее событие доходит до каждого пользователя")
    void broadcastsEventToEveryUser() throws Exception {
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0));
        final String text = UUID.randomUUID().toString();
        audience.stream(31L).subscribe();
        final Future<List<String>> received = audience.stream(32L)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        audience.broadcast(ServerSentEvent.builder(text).build());
        MatcherAssert.assertThat(
            "общее событие дошло не до каждого пользователя",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("пользователь забывается, когда закрыт его последний поток")
    void forgetsUserAfterLastStreamCloses() {
        final ConcurrentMap<Long, Crowd> map = new ConcurrentHashMap<>(0);
        final Audience audience = new SinkAudience(map);
        audience.stream(41L).subscribe().dispose();
        MatcherAssert.assertThat(
            "пользователь с закрытыми потоками остался в аудитории",
            map,
            Matchers.anEmptyMap()
        );
    }

    @Test
    @DisplayName("пользователь остаётся, пока открыт другой его поток")
    void keepsUserWhileAnotherStreamIsOpen() {
        final ConcurrentMap<Long, Crowd> map = new ConcurrentHashMap<>(0);
        final Audience audience = new SinkAudience(map);
        audience.stream(51L).subscribe();
        audience.stream(51L).subscribe().dispose();
        MatcherAssert.assertThat(
            "пользователь с открытым потоком пропал из аудитории",
            map,
            Matchers.hasKey(51L)
        );
    }

    @Test
    @DisplayName("событие для неподключённого пользователя не регистрирует его")
    void ignoresEventForDisconnectedUser() {
        final ConcurrentMap<Long, Crowd> map = new ConcurrentHashMap<>(0);
        new SinkAudience(map).deliver(61L, ServerSentEvent.builder("никому").build());
        MatcherAssert.assertThat(
            "пользователь появился в аудитории из-за пришедшего ему события",
            map,
            Matchers.anEmptyMap()
        );
    }

    @RepeatedTest(10)
    @DisplayName("после параллельных подключений и отключений не остаётся пользователей")
    void forgetsUsersAfterConcurrentChurn() {
        final ConcurrentMap<Long, Crowd> map = new ConcurrentHashMap<>(0);
        final Audience audience = new SinkAudience(map);
        Flux.range(0, 2000)
            .parallel(8)
            .runOn(Schedulers.boundedElastic()).doOnNext(
                num -> audience.stream(num % 3L).take(Duration.ofMillis(num % 2L)).blockLast()
            )
            .sequential()
            .blockLast(Duration.ofSeconds(30L));
        MatcherAssert.assertThat(
            "после параллельной работы в аудитории остались пользователи",
            map,
            Matchers.anEmptyMap()
        );
    }
}
