/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04.extended;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация с аннотациями-аспектами и счётчиками настоящих вызовов.
 *
 * <p>Счётчики нужны, чтобы отличить «аспект сработал» от «метод просто вернул
 * то же значение»: без них кэш и заглушку невозможно проверить.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ConstructorShouldDoInitialization")
public final class RealPriceService implements PriceService {

    /**
     * Значение {@code prices}.
     */
    private final AtomicInteger prices = new AtomicInteger();

    /**
     * Число попыток.
     */
    private final AtomicInteger attempts = new AtomicInteger();

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public RealPriceService() {
        // нечего инициализировать
    }

    @Override
    @Cached
    public int price(final String sku) {
        this.prices.incrementAndGet();
        return sku.length() * 10;
    }

    @Override
    public int plain(final int value) {
        return value * 2;
    }

    @Override
    @Cached
    public int priceTwice(final String sku) {
        return this.price(sku) + this.price(sku);
    }

    /**
     * Сколько раз цель действительно посчитала цену.
     * @return Сколько раз цель действительно посчитала цену
     */
    public int calls() {
        return this.prices.get();
    }

    @Override
    @Retry(attempts = 3)
    @Timed
    public int flaky() {
        if (this.attempts.incrementAndGet() < 3) {
            throw new IllegalStateException("временный сбой");
        }
        return 42;
    }

    @Override
    @Stubbed("RUB")
    public String currency() {
        throw new AssertionError("цель не должна вызываться при @Stubbed");
    }
}
