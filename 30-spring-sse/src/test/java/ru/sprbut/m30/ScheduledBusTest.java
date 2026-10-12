/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.IntStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/**
 * Тесты {@link ScheduledBus}.
 * @since 1.0
 */
final class ScheduledBusTest {

    @Test
    @DisplayName("события личного канала доставляются в потоке пула")
    void deliversUserEventsInPool() {
        final Scheduler scheduler = ScheduledBusTest.scheduler(1, 10);
        try {
            final Bus bus = new ScheduledBus(new FakeBus(), scheduler);
            bus.publish(10L, new TextNotice("user-event", "лично")).block(Duration.ofSeconds(5L));
            MatcherAssert.assertThat(
                "событие личного канала доставлено не в потоке пула",
                bus.user(10L).map(event -> Thread.currentThread().getName())
                    .blockFirst(Duration.ofSeconds(5L)),
                Matchers.startsWith("delivery-")
            );
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    @DisplayName("события общего канала доставляются в потоке пула по порядку")
    void deliversBroadcastsInOrder() {
        final Scheduler scheduler = ScheduledBusTest.scheduler(4, 10);
        try {
            final Bus bus = new ScheduledBus(new FakeBus(), scheduler);
            for (int idx = 0; idx < 50; ++idx) {
                bus.publish(new TextNotice("broadcast", String.valueOf(idx)))
                    .block(Duration.ofSeconds(5L));
            }
            MatcherAssert.assertThat(
                "события общего канала доставлены не по порядку",
                bus.broadcasts().take(50L).map(ServerSentEvent::data).collectList()
                    .block(Duration.ofSeconds(5L)),
                Matchers.equalTo(
                    IntStream.range(0, 50).mapToObj(String::valueOf).toList()
                )
            );
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    @DisplayName("переполненный пул обрывает подписку ошибкой")
    void failsSubscriptionWhenPoolIsFull() {
        final Scheduler scheduler = ScheduledBusTest.scheduler(1, 0);
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            scheduler.schedule(
                () -> {
                    try {
                        latch.await();
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            );
            final Bus bus = new ScheduledBus(new FakeBus(), scheduler);
            bus.publish(11L, new TextNotice("user-event", "лично")).block(Duration.ofSeconds(5L));
            StepVerifier.create(bus.user(11L))
                .expectError(RejectedExecutionException.class)
                .verify(Duration.ofSeconds(5L));
        } finally {
            latch.countDown();
            scheduler.dispose();
        }
    }

    @Test
    @DisplayName("публикация проходит к исходным каналам")
    void passesPublishThrough() {
        final Scheduler scheduler = ScheduledBusTest.scheduler(1, 10);
        try {
            final FakeBus origin = new FakeBus();
            new ScheduledBus(origin, scheduler)
                .publish(new TextNotice("broadcast", "всем")).block(Duration.ofSeconds(5L));
            MatcherAssert.assertThat(
                "публикация не дошла до исходных каналов",
                origin.broadcasts().take(1L).map(ServerSentEvent::data).collectList()
                    .block(Duration.ofSeconds(5L)),
                Matchers.equalTo(List.of("всем"))
            );
        } finally {
            scheduler.dispose();
        }
    }

    private static Scheduler scheduler(final int size, final int queue) {
        return Schedulers.fromExecutorService(
            new Pool(size, size, queue, Duration.ofSeconds(1L))
                .executor("delivery", new SimpleMeterRegistry()),
            "delivery"
        );
    }
}
