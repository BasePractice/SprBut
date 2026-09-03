/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m20.extended;

import java.util.List;

/**
 * Карточка одного маршрута: что вызовется и по какому запросу.
 *
 * @param methods HTTP-методы, на которые отзывается маршрут
 * @param patterns Шаблоны пути
 * @param handler Метод, который будет вызван
 * @since 1.0
 */
public record RouteCard(List<String> methods, List<String> patterns, String handler) {

    /**
     * Компактный конструктор.
     *
     * <p>Копии списков снимаются здесь: карточка обязана быть неизменяемой.</p>
     */
    public RouteCard {
        methods = List.copyOf(methods);
        patterns = List.copyOf(patterns);
    }
}
