/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.Array;

/**
 * Элементы массива, доступные без приведения типа.
 *
 * <p>{@code Object[]} здесь не подошёл бы: массив примитивов им не является,
 * и {@code int[]} пришлось бы обрабатывать отдельной веткой. {@link Array}
 * работает с любым массивом одинаково — за это и платят потерей типизации.</p>
 *
 * @since 1.0
 */
public final class ArrayValue {

    /**
     * Значение {@code array}.
     */
    private final Object array;

    /**
     * Основной конструктор.
     * @param array Значение {@code array}
     */
    public ArrayValue(final Object array) {
        this.array = array;
    }

    /**
     * Элемент по индексу.
     * @param index Индекс
     * @return Элемент по индексу
     */
    public Object element(final int index) {
        return Array.get(this.array, index);
    }

    /**
     * Записывает элемент по индексу.
     * @param index Индекс
     * @param value Значение
     */
    public void assign(final int index, final Object value) {
        Array.set(this.array, index, value);
    }

    /**
     * Длина массива.
     * @return Длина массива
     */
    public int length() {
        return Array.getLength(this.array);
    }
}
