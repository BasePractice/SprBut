/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m15.aop;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

/**
 * Реализация {@link DiscountService}. Именно её Spring обернёт JDK-прокси.
 * @since 1.0
 */
@Service
public final class StandardDiscountService implements DiscountService {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public StandardDiscountService() {
        // нечего инициализировать
    }

    @Override
    public BigDecimal calculate(final BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.95"));
    }

    @Override
    public String name() {
        return "standard";
    }
}
