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
    @DisplayName("событие личного канала доходит до подключённого пользователя")
    void deliversChannelEventToConnectedUser() throws Exception {
        final FakeBus bus = new FakeBus();
        final long user = ThreadLocalRandom.current().nextLong(1L, 1000L);
        final String text = UUID.randomUUID().toString();
        final Future<List<String>> received = new SinkAudience(new ConcurrentHashMap<>(0), bus)
            .stream(user).map(ServerSentEvent::data).take(1L).collectList().toFuture();
        bus.publish(user, new TextNotice("user-event", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "событие личного канала не дошло до подключённого пользователя",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("событие другого пользователя не попадает в чужой поток")
    void doesntDeliverEventOfAnotherUser() throws Exception {
        final FakeBus bus = new FakeBus();
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), bus);
        final String text = UUID.randomUUID().toString();
        final Future<List<String>> received = audience.stream(11L)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        audience.stream(12L).subscribe();
        bus.publish(12L, new TextNotice("user-event", "чужое")).block(Duration.ofSeconds(5L));
        bus.publish(11L, new TextNotice("user-event", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "в поток попало событие другого пользователя",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("событие доходит до каждого потока одного пользователя")
    void deliversEventToEveryStreamOfUser() throws Exception {
        final FakeBus bus = new FakeBus();
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), bus);
        final String text = UUID.randomUUID().toString();
        audience.stream(21L).subscribe();
        final Future<List<String>> received = audience.stream(21L)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        bus.publish(21L, new TextNotice("user-event", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "второй поток того же пользователя не получил событие",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("общее событие доходит до каждого пользователя")
    void broadcastsEventToEveryUser() throws Exception {
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), new FakeBus());
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
    @DisplayName("пользователь подписывается на личный канал один раз на все свои потоки")
    void subscribesChannelOncePerUser() {
        final FakeBus bus = new FakeBus();
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), bus);
        audience.stream(35L).subscribe();
        audience.stream(35L).subscribe();
        audience.stream(35L).subscribe();
        MatcherAssert.assertThat(
            "на каждый поток пользователя открыта своя подписка на канал",
            bus.listeners(35L),
            Matchers.equalTo(1)
        );
    }

    @Test
    @DisplayName("личный канал отписывается, когда закрыт последний поток пользователя")
    void unsubscribesChannelAfterLastStreamCloses() {
        final FakeBus bus = new FakeBus();
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), bus);
        audience.stream(36L).subscribe().dispose();
        audience.stream(36L).subscribe().dispose();
        MatcherAssert.assertThat(
            "подписка на канал пережила последний поток пользователя",
            bus.listeners(36L),
            Matchers.equalTo(0)
        );
    }

    @Test
    @DisplayName("личный канал остаётся подписанным, пока открыт другой поток")
    void keepsChannelWhileAnotherStreamIsOpen() {
        final FakeBus bus = new FakeBus();
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), bus);
        audience.stream(37L).subscribe();
        audience.stream(37L).subscribe().dispose();
        MatcherAssert.assertThat(
            "подписка на канал закрылась при открытом потоке пользователя",
            bus.listeners(37L),
            Matchers.equalTo(1)
        );
    }

    @Test
    @DisplayName("пользователь забывается, когда закрыт его последний поток")
    void forgetsUserAfterLastStreamCloses() {
        final ConcurrentMap<Long, Crowd> map = new ConcurrentHashMap<>(0);
        new SinkAudience(map, new FakeBus()).stream(41L).subscribe().dispose();
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
        final Audience audience = new SinkAudience(map, new FakeBus());
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
        final FakeBus bus = new FakeBus();
        new SinkAudience(map, bus).broadcast(ServerSentEvent.builder("всем").build());
        bus.publish(61L, new TextNotice("user-event", "никому")).block(Duration.ofSeconds(5L));
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
        final Audience audience = new SinkAudience(map, new FakeBus());
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

    @RepeatedTest(10)
    @DisplayName("после параллельных подключений и отключений не остаётся подписок на каналы")
    void unsubscribesChannelsAfterConcurrentChurn() {
        final FakeBus bus = new FakeBus();
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0), bus);
        Flux.range(0, 2000)
            .parallel(8)
            .runOn(Schedulers.boundedElastic()).doOnNext(
                num -> audience.stream(num % 3L).take(Duration.ofMillis(num % 2L)).blockLast()
            )
            .sequential()
            .blockLast(Duration.ofSeconds(30L));
        MatcherAssert.assertThat(
            "после параллельной работы остались подписки на личные каналы",
            List.of(bus.listeners(0L), bus.listeners(1L), bus.listeners(2L)),
            Matchers.contains(0, 0, 0)
        );
    }
}
