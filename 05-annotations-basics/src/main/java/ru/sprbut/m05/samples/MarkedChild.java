/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.samples;

/**
 * Подкласс, до которого {@code @Marker} не доходит: наследование аннотаций
 * включается самой аннотацией, а не иерархией.
 * @since 1.0
 */
public class MarkedChild extends MarkedParent {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MarkedChild() {
        // нечего инициализировать
    }

}
