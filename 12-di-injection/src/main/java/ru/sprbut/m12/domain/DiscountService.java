/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.domain;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Вторая зависимость — чтобы у сервиса их было больше одной.
 * @since 1.0
 */
@Component
public class DiscountService {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public DiscountService() {
        // нечего инициализировать
    }

    /**
     * Применение.
     * @param amount Сумма
     * @param vip Признак привилегированного клиента
     * @return Применение
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public BigDecimal apply(final BigDecimal amount, final boolean vip) {
        return vip
                ? amount.multiply(new BigDecimal("0.9")).setScale(2, RoundingMode.HALF_UP)
                : amount;
    }
}
