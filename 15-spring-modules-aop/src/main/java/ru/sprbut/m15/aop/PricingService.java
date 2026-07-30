package ru.sprbut.m15.aop;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Целевой бин <b>без интерфейса</b>. Слайд 123: «CGLIB-подкласс — если интерфейса нет».
 * <p>
 * Здесь же живёт демонстрация ключевого ограничения (слайд 124):
 * {@link #calculateTwice} вызывает {@link #calculate} через {@code this},
 * а не через прокси — и аспект такой вызов не видит.
 */
@Service
public class PricingService {

    private int calls;

    public BigDecimal calculate(BigDecimal net) {
        calls++;
        return net.multiply(new BigDecimal("1.20"));
    }

    /**
     * Self-invocation: внутренний вызов идёт напрямую по ссылке {@code this},
     * минуя прокси. Аспект на {@code calculate} здесь не сработает.
     */
    public BigDecimal calculateTwice(BigDecimal net) {
        return calculate(net).add(calculate(net));
    }

    public BigDecimal failing(BigDecimal net) {
        throw new IllegalArgumentException("расчёт невозможен для " + net);
    }

    public int calls() {
        return calls;
    }

    public void reset() {
        calls = 0;
    }
}
