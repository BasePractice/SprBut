/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m15.aop;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * Целевой бин <b>без интерфейса</b>. Слайд 123: «CGLIB-подкласс — если интерфейса нет».
 *
 * <p>Здесь же живёт демонстрация ключевого ограничения (слайд 124):
 * {@link #calculateTwice} вызывает {@link #calculate} через {@code this},
 * а не через прокси — и аспект такой вызов не видит.</p>
 *
 * @since 1.0
 */
@Service
public class PricingService {
    /**
     * Число вызовов.
     */
    private int calls;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public PricingService() {
        // нечего инициализировать
    }

    /**
     * Вычисление.
     * @param net Сумма без налога
     * @return Вычисление
     */
    public BigDecimal calculate(final BigDecimal net) {
        this.calls++;
        return net.multiply(new BigDecimal("1.20"));
    }

    /**
     * Self-invocation: внутренний вызов идёт напрямую по ссылке {@code this},
     * минуя прокси. Аспект на {@code calculate} здесь не сработает.
     * @param net Сумма без налога
     * @return Self-invocation: внутренний вызов идёт напрямую по ссылке {@code this}, минуя прокси. Аспект на {@code calculate} здесь не сработает
     */
    public BigDecimal calculateTwice(final BigDecimal net) {
        return this.calculate(net).add(this.calculate(net));
    }

    /**
     * Падающий элемент.
     * @param net Сумма без налога
     * @return Падающий элемент
     */
    public BigDecimal failing(final BigDecimal net) {
        throw new IllegalArgumentException("расчёт невозможен для " + net);
    }

    /**
     * Число вызовов.
     * @return Число вызовов
     */
    public int calls() {
        return this.calls;
    }

    /**
     * Сброс состояния.
     */
    public void reset() {
        this.calls = 0;
    }
}
