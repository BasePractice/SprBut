package ru.sprbut.m04.extended;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация с аннотациями-аспектами и счётчиками настоящих вызовов.
 * <p>
 * Счётчики нужны, чтобы отличить «аспект сработал» от «метод просто вернул
 * то же значение»: без них кэш и заглушку невозможно проверить.
 */
public final class RealPriceService implements PriceService {

    private final AtomicInteger prices = new AtomicInteger();

    private final AtomicInteger attempts = new AtomicInteger();

    @Override
    @Cached
    public int price(String sku) {
        this.prices.incrementAndGet();
        return sku.length() * 10;
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

    @Override
    public int plain(int value) {
        return value * 2;
    }

    @Override
    @Cached
    public int priceTwice(String sku) {
        return price(sku) + price(sku);
    }

    /**
     * Сколько раз цель действительно посчитала цену.
     */
    public int calls() {
        return this.prices.get();
    }
}
