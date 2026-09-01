/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.ambiguous;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Потребитель, который называет нужную реализацию по имени.
 *
 * <p>{@code @Qualifier} работает на стороне точки внедрения — в отличие от
 * {@code @Primary}, выбор делает тот, кто зависимость получает, а не тот,
 * кто её объявляет.</p>
 *
 * @since 1.0
 */
@Service
public final class EconomyDelivery {

    /**
     * Служба доставки, выбранная квалификатором.
     */
    private final Shipper shipper;

    /**
     * Основной конструктор.
     * @param shipper Служба доставки, выбранная квалификатором
     */
    public EconomyDelivery(@Qualifier("economy") final Shipper shipper) {
        this.shipper = shipper;
    }

    /**
     * Срок доставки экономного канала.
     * @return Срок доставки в днях
     */
    public int promise() {
        return this.shipper.days();
    }
}
