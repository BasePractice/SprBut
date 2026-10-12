/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/**
 * Тесты {@link Pool} и {@link Pools}.
 * @since 1.0
 */
final class PoolTest {

    @Test
    @DisplayName("пул отвергает задание, когда заняты все потоки и полна очередь")
    void rejectsTaskWhenFull() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecutorService pool = PoolTest.pool(1, "test", new SimpleMeterRegistry())) {
            PoolTest.occupy(pool, latch, 2);
            Assertions.assertThrows(
                RejectedExecutionException.class,
                () -> pool.execute(() -> PoolTest.await(latch)),
                "пул не отверг задание сверх потоков и очереди"
            );
            latch.countDown();
        }
    }

    @Test
    @DisplayName("пул считает отвергнутые задания в метриках")
    void countsRejectedTasks() throws Exception {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecutorService pool = PoolTest.pool(0, "test", registry)) {
            PoolTest.occupy(pool, latch, 1);
            Assertions.assertThrows(
                RejectedExecutionException.class,
                () -> pool.execute(() -> PoolTest.await(latch))
            );
            latch.countDown();
        }
        MatcherAssert.assertThat(
            "пул не посчитал отвергнутое задание",
            registry.get("executor.rejected").tag("name", "test").counter().count(),
            Matchers.equalTo(1.0)
        );
    }

    @Test
    @DisplayName("пул показывает в метриках длину очереди")
    void exposesQueuedTasks() throws Exception {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecutorService pool = PoolTest.pool(5, "test", registry)) {
            PoolTest.occupy(pool, latch, 3);
            MatcherAssert.assertThat(
                "пул ошибся в длине очереди",
                registry.get("executor.queued").tag("name", "test").gauge().value(),
                Matchers.equalTo(2.0)
            );
            latch.countDown();
        }
    }

    @Test
    @DisplayName("пул показывает в метриках остаток очереди")
    void exposesRemainingQueue() throws Exception {
        final MeterRegistry registry = new SimpleMeterRegistry();
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecutorService pool = PoolTest.pool(5, "test", registry)) {
            PoolTest.occupy(pool, latch, 3);
            MatcherAssert.assertThat(
                "пул ошибся в остатке очереди",
                registry.get("executor.queue.remaining").tag("name", "test").gauge().value(),
                Matchers.equalTo(3.0)
            );
            latch.countDown();
        }
    }

    @Test
    @DisplayName("потоки пула названы по имени пула")
    void namesThreadsAfterPool() throws Exception {
        try (ExecutorService pool = PoolTest.pool(1, "named", new SimpleMeterRegistry())) {
            MatcherAssert.assertThat(
                "поток пула назван не по имени пула",
                pool.submit(() -> Thread.currentThread().getName()).get(),
                Matchers.startsWith("named-")
            );
        }
    }

    @Test
    @DisplayName("свойства пулов берутся из конфигурации, а пропущенные — по умолчанию")
    void bindsPoolsFromProperties() {
        MatcherAssert.assertThat(
            "свойства пулов связаны неверно",
            new Binder(
                new MapConfigurationPropertySource(
                    Map.of(
                        "sse.pools.commands.core-size", "2",
                        "sse.pools.commands.max-size", "16",
                        "sse.pools.commands.queue-capacity", "50",
                        "sse.pools.commands.keep-alive", "10s"
                    )
                )
            ).bindOrCreate("sse.pools", Pools.class),
            Matchers.equalTo(
                new Pools(
                    new Pool(4, 4, 1000, Duration.ofSeconds(60L)),
                    new Pool(2, 16, 50, Duration.ofSeconds(10L))
                )
            )
        );
    }

    private static ExecutorService pool(
        final int queue,
        final String name,
        final MeterRegistry registry
    ) {
        return new Pool(1, 1, queue, Duration.ofSeconds(1L)).executor(name, registry);
    }

    private static void occupy(
        final ExecutorService pool,
        final CountDownLatch latch,
        final int tasks
    ) {
        for (int idx = 0; idx < tasks; ++idx) {
            pool.execute(() -> PoolTest.await(latch));
        }
    }

    private static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
