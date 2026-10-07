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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import tools.jackson.databind.json.JsonMapper;

/**
 * Тесты {@link Relay}.
 * @since 1.0
 */
final class RelayTest {

    @Test
    @DisplayName("ретранслятор доставляет опубликованное уведомление")
    void deliversPublishedNotice() {
        final FakeBus bus = new FakeBus();
        final FakeAudience audience = new FakeAudience();
        final String text = UUID.randomUUID().toString();
        new Relay(bus, audience, Duration.ofMillis(10L)).start();
        bus.publish(new UserNotice(9L, text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор не доставил опубликованное уведомление",
            audience.deliveries(),
            Matchers.contains(String.format("9 user-event %s", text))
        );
    }

    @Test
    @DisplayName("ретранслятор пропускает битое уведомление и работает дальше")
    void skipsBrokenNotice() {
        final FakeBus bus = new FakeBus();
        final FakeAudience audience = new FakeAudience();
        final String text = UUID.randomUUID().toString();
        new Relay(bus, audience, Duration.ofMillis(10L)).start();
        bus.publish(new ParsedNotice("{битый", new JsonMapper())).block(Duration.ofSeconds(5L));
        bus.publish(new BroadcastNotice(text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор остановился на битом уведомлении",
            audience.deliveries(),
            Matchers.contains(String.format("all broadcast %s", text))
        );
    }

    @Test
    @DisplayName("ретранслятор переподписывается после обрыва канала")
    void resubscribesAfterChannelBreaks() throws Exception {
        final FakeBus bus = new FakeBus(2);
        final Audience audience = new SinkAudience(new ConcurrentHashMap<>(0));
        final String text = UUID.randomUUID().toString();
        final Future<List<String>> received = audience.stream(8L)
            .map(ServerSentEvent::data).take(1L).collectList().toFuture();
        new Relay(bus, audience, Duration.ofMillis(10L)).start();
        bus.publish(new UserNotice(8L, text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор не переподписался после обрыва канала",
            received.get(5L, TimeUnit.SECONDS),
            Matchers.contains(text)
        );
    }

    @Test
    @DisplayName("ретранслятор работает после запуска")
    void runsAfterStart() {
        final Relay relay = new Relay(new FakeBus(), new FakeAudience(), Duration.ofMillis(10L));
        relay.start();
        MatcherAssert.assertThat(
            "ретранслятор не работает после запуска",
            relay.isRunning(),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("ретранслятор не работает после остановки")
    void doesntRunAfterStop() {
        final Relay relay = new Relay(new FakeBus(), new FakeAudience(), Duration.ofMillis(10L));
        relay.start();
        relay.stop();
        MatcherAssert.assertThat(
            "ретранслятор всё ещё работает после остановки",
            relay.isRunning(),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("остановленный ретранслятор ничего не доставляет")
    void doesntDeliverAfterStop() {
        final FakeBus bus = new FakeBus();
        final FakeAudience audience = new FakeAudience();
        final Relay relay = new Relay(bus, audience, Duration.ofMillis(10L));
        relay.start();
        relay.stop();
        bus.publish(new BroadcastNotice("поздно")).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор доставил уведомление после остановки",
            audience.deliveries(),
            Matchers.empty()
        );
    }
}
