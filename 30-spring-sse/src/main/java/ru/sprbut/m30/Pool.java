/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * Настройки пула потоков с ограниченной очередью заданий.
 *
 * <p>{@link ThreadPoolExecutor} сначала заполняет ядро, затем очередь, и только
 * при полной очереди добавляет потоки сверх ядра, вплоть до максимума. Когда
 * заняты все потоки и полна очередь, задание отвергается: пул не копит
 * работу без предела, а сообщает о перегрузке вызвавшему.</p>
 *
 * @param coreSize Число потоков, живущих всегда
 * @param maxSize Предельное число потоков
 * @param queueCapacity Ёмкость очереди заданий; ноль — передача из рук в руки
 * @param keepAlive Сколько живёт простаивающий поток сверх ядра
 * @since 1.0
 */
public record Pool(
    @DefaultValue("4") int coreSize,
    @DefaultValue("4") int maxSize,
    @DefaultValue("1000") int queueCapacity,
    @DefaultValue("60s") Duration keepAlive
) {

    /**
     * Пул потоков с метриками.
     *
     * <p>Метрики Micrometer {@code executor.*} с тегом {@code name} показывают
     * потоки, очередь, выполненные задания и время их выполнения; счётчик
     * {@code executor.rejected} — сколько заданий отвергнуто.</p>
     *
     * @param name Имя пула: префикс имён потоков и тег метрик
     * @param registry Реестр метрик
     * @return Пул, который закрывает тот, кто его создал
     */
    ExecutorService executor(final String name, final MeterRegistry registry) {
        final Counter rejected = Counter.builder("executor.rejected")
            .description("Сколько заданий пул отверг: заняты все потоки и полна очередь")
            .baseUnit("tasks")
            .tag("name", name)
            .register(registry);
        return ExecutorServiceMetrics.monitor(
            registry,
            new ThreadPoolExecutor(
                this.coreSize,
                this.maxSize,
                this.keepAlive.toMillis(),
                TimeUnit.MILLISECONDS,
                this.queue(),
                new CustomizableThreadFactory(String.format("%s-", name)),
                (task, pool) -> {
                    if (!pool.isShutdown()) {
                        rejected.increment();
                    }
                    throw new RejectedExecutionException(
                        String.format(
                            "Пул %s отверг задание: заняты все %d потоков и полна очередь",
                            name, pool.getMaximumPoolSize()
                        )
                    );
                }
            ),
            name
        );
    }

    private BlockingQueue<Runnable> queue() {
        final BlockingQueue<Runnable> queue;
        if (this.queueCapacity == 0) {
            queue = new SynchronousQueue<>();
        } else {
            queue = new ArrayBlockingQueue<>(this.queueCapacity);
        }
        return queue;
    }
}
