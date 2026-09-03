/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.scopes;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Выдаёт возрастающие номера. Нужен, чтобы отличить один экземпляр
 * prototype-бина от другого, не заводя статического счётчика.
 * @since 1.0
 */
public final class Serial {

    /**
     * Счётчик выданных номеров.
     */
    private final AtomicInteger counter;

    /**
     * Вторичный конструктор со счётчиком с нуля.
     */
    public Serial() {
        this(new AtomicInteger());
    }

    /**
     * Основной конструктор.
     * @param counter Счётчик выданных номеров
     */
    public Serial(final AtomicInteger counter) {
        this.counter = counter;
    }

    /**
     * Следующий номер в последовательности.
     * @return Следующий номер
     */
    public int next() {
        return this.counter.incrementAndGet();
    }
}
