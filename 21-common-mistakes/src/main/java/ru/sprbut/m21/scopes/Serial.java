package ru.sprbut.m21.scopes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Выдаёт возрастающие номера. Нужен, чтобы отличить один экземпляр
 * prototype-бина от другого, не заводя статического счётчика.
 */
public final class Serial {

    private final AtomicInteger counter;

    public Serial() {
        this(new AtomicInteger());
    }

    public Serial(AtomicInteger counter) {
        this.counter = counter;
    }

    /**
     * Следующий номер в последовательности.
     */
    public int next() {
        return this.counter.incrementAndGet();
    }
}
