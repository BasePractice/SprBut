/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m22.extended;

import java.util.List;

/**
 * Карточка одной цепочки фильтров: к чему она применяется и из чего состоит.
 *
 * @param matcher Условие, по которому цепочка берётся в работу
 * @param filters Фильтры в порядке прохождения запроса
 * @since 1.0
 */
public record ChainCard(String matcher, List<String> filters) {

    /**
     * Компактный конструктор.
     *
     * <p>Копия списка снимается здесь: карточка обязана быть неизменяемой.</p>
     */
    public ChainCard {
        filters = List.copyOf(filters);
    }
}
