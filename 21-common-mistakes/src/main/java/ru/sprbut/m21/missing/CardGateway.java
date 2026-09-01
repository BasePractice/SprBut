/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m21.missing;

/**
 * Реализация платёжного канала, которая чинит {@link MissingBeanConfig}.
 *
 * <p>Класс существовал всё это время — не хватало только строчки, объявляющей его бином.</p>
 *
 * @since 1.0
 */
public final class CardGateway implements PaymentGateway {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public CardGateway() {
        // нечего инициализировать
    }

    @Override
    public String channel() {
        return "card";
    }
}
