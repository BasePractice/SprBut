/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m15.aop;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/** Реализация {@link DiscountService}. Именно её Spring обернёт JDK-прокси. */
@Service
public class StandardDiscountService implements DiscountService {

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
