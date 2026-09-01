/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.ambiguous;

import org.springframework.stereotype.Service;

/**
 * Потребитель, из-за которого неоднозначность становится ошибкой.
 *
 * <p>Просит {@link Shipper} «вообще», не уточняя какой.</p>
 *
 * @since 1.0
 */
@Service
public final class DeliveryService {

    /**
     * Служба доставки, внедрённая контейнером.
     */
    private final Shipper shipper;

    /**
     * Основной конструктор.
     * @param shipper Служба доставки, внедрённая контейнером
     */
    public DeliveryService(final Shipper shipper) {
        this.shipper = shipper;
    }

    /**
     * Срок доставки, обещанный внедрённой службой.
     * @return Срок доставки в днях
     */
    public int promise() {
        return this.shipper.days();
    }
}
