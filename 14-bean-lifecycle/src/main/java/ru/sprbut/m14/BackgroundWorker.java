/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m14;

import org.springframework.context.SmartLifecycle;

/**
 * Шаг 7: «Бин готов, {@code SmartLifecycle.start}».
 *
 * <p>{@link SmartLifecycle} — это <b>не</b> инициализация бина. Инициализация
 * ({@code @PostConstruct}) заканчивается, когда объект готов к использованию.
 * {@code start()} вызывается позже — когда готов <b>весь контекст</b>.</p>
 *
 * <p>Разница принципиальна для всего, что начинает работать самостоятельно:
 * слушатели очередей, планировщики, сетевые серверы. В {@code @PostConstruct}
 * такое запускать нельзя — остальные бины могут быть ещё не созданы.</p>
 *
 * @since 1.0
 */
public final class BackgroundWorker implements SmartLifecycle {

    /**
     * Значение {@code running}.
     */
    private volatile boolean running;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public BackgroundWorker() {
        // нечего инициализировать
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        this.running = true;
        LifecycleLog.record("7-smartLifecycle-start:backgroundWorker");
    }

    @Override
    public void stop() {
        this.running = false;
        LifecycleLog.record("9-smartLifecycle-stop:backgroundWorker");
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
