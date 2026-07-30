package ru.sprbut.m14;

import org.springframework.context.SmartLifecycle;

/**
 * Шаг 7: «Бин готов, {@code SmartLifecycle.start}».
 * <p>
 * {@link SmartLifecycle} — это <b>не</b> инициализация бина. Инициализация
 * ({@code @PostConstruct}) заканчивается, когда объект готов к использованию.
 * {@code start()} вызывается позже — когда готов <b>весь контекст</b>.
 * <p>
 * Разница принципиальна для всего, что начинает работать самостоятельно:
 * слушатели очередей, планировщики, сетевые серверы. В {@code @PostConstruct}
 * такое запускать нельзя — остальные бины могут быть ещё не созданы.
 */
public class BackgroundWorker implements SmartLifecycle {

    private volatile boolean running;

    /** Порядок запуска среди других SmartLifecycle: меньше — раньше. */
    @Override
    public int getPhase() {
        return 0;
    }

    /** {@code true} — контейнер запустит сам, при готовности контекста. */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        running = true;
        LifecycleLog.record("7-smartLifecycle-start:backgroundWorker");
    }

    @Override
    public void stop() {
        running = false;
        LifecycleLog.record("9-smartLifecycle-stop:backgroundWorker");
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
