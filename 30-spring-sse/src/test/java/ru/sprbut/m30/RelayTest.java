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

/**
 * Тесты {@link Relay}.
 * @since 1.0
 */
final class RelayTest {

    @Test
    @DisplayName("ретранслятор рассылает событие общего канала всем пользователям")
    void broadcastsEventOfBroadcastChannel() {
        final FakeBus bus = new FakeBus();
        final FakeAudience audience = new FakeAudience();
        final String text = UUID.randomUUID().toString();
        new Relay(bus, audience).start();
        bus.publish(new TextNotice("broadcast", text)).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор не разослал событие общего канала",
            audience.deliveries(),
            Matchers.contains(String.format("all broadcast %s", text))
        );
    }

    @Test
    @DisplayName("ретранслятор не рассылает личные события")
    void doesntBroadcastUserEvent() {
        final FakeBus bus = new FakeBus();
        final FakeAudience audience = new FakeAudience();
        new Relay(bus, audience).start();
        bus.publish(7L, new TextNotice("user-event", "лично")).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор разослал всем личное событие",
            audience.deliveries(),
            Matchers.empty()
        );
    }

    @Test
    @DisplayName("ретранслятор подписан на общий канал после запуска")
    void runsAfterStart() {
        final Relay relay = new Relay(new FakeBus(), new FakeAudience());
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
        final Relay relay = new Relay(new FakeBus(), new FakeAudience());
        relay.start();
        relay.stop();
        MatcherAssert.assertThat(
            "ретранслятор всё ещё работает после остановки",
            relay.isRunning(),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("остановленный ретранслятор ничего не рассылает")
    void doesntBroadcastAfterStop() {
        final FakeBus bus = new FakeBus();
        final FakeAudience audience = new FakeAudience();
        final Relay relay = new Relay(bus, audience);
        relay.start();
        relay.stop();
        bus.publish(new TextNotice("broadcast", "поздно")).block(Duration.ofSeconds(5L));
        MatcherAssert.assertThat(
            "ретранслятор разослал событие после остановки",
            audience.deliveries(),
            Matchers.empty()
        );
    }
}
