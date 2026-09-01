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

/** Простая зависимость, которую будут внедрять тремя способами. */
@Component
public class TaxService {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TaxService() {
        // нечего инициализировать
    }

    /**
     * Начисление НДС.
     * @param net Сумма без налога
     * @return Начисление НДС
     */
    public BigDecimal applyVat(final BigDecimal net) {
        return net.multiply(new BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP);
    }
}
