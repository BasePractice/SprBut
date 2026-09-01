/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Вторая зависимость сервиса — чтобы граф был не тривиальным.
 * @param vatRate Параметр типа
 * @since 1.0
 */
public record PriceCalculator(BigDecimal vatRate) {

    /**
     * Сумма с НДС.
     * @param net Сумма без налога
     * @return Сумма с НДС
     */
    public BigDecimal withVat(final BigDecimal net) {
        return net.multiply(BigDecimal.ONE.add(this.vatRate)).setScale(2, RoundingMode.HALF_UP);
    }
}
