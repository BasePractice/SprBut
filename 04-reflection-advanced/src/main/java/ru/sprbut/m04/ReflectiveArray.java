/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.Array;

/**
 * Слайд 36: массив с типом элемента, известным только в runtime.
 *
 * <p>Обычный {@code new T[n]} в Java невозможен из-за стирания — остаётся
 * {@link Array#newInstance}. Ровно этим пользуется каждый десериализатор,
 * которому нужно построить массив нужного типа по метаданным.</p>
 *
 * @since 1.0
 */
public final class ReflectiveArray {

    /**
     * Компонент.
     */
    private final Class<?> component;

    /**
     * Основной конструктор.
     * @param component Компонент
     */
    public ReflectiveArray(final Class<?> component) {
        this.component = component;
    }

    /**
     * Одномерный массив.
     * @param length Длина
     * @return Одномерный массив
     */
    public Object single(final int length) {
        return Array.newInstance(this.component, length);
    }

    /**
     * Многомерный массив создаётся тем же API — просто размеров больше одного.
     * @param columns Значение {@code columns}
     * @param rows Строки
     * @return Многомерный массив создаётся тем же API — просто размеров больше одного
     */
    public Object matrix(final int rows, final int columns) {
        return Array.newInstance(this.component, rows, columns);
    }
}
