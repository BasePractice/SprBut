/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.members;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Значение элемента аннотации, пригодное для печати.
 *
 * <p>Массив без такой обработки печатается своим хэшем, а {@code Class} —
 * полным именем с пакетом. И то и другое делает отчёт нечитаемым ровно там,
 * где он нужен: при разборе чужой конфигурации.</p>
 *
 * @since 1.0
 */
public final class Described {
    /**
     * Значение.
     */
    private final Object value;

    /**
     * Основной конструктор.
     * @param value Значение
     */
    public Described(final Object value) {
        this.value = value;
    }

    private String array() {
        final int length = Array.getLength(this.value);
        final Object[] items = new Object[length];
        for (int index = 0; index < length; index++) {
            items[index] = new Described(Array.get(this.value, index)).text();
        }
        return Arrays.toString(items);
    }

    /**
     * Читаемое представление значения.
     * @return Читаемое представление значения
     */
    public Object text() {
        if (this.value == null) {
            return null;
        }
        if (this.value instanceof Class<?> type) {
            return type.getSimpleName();
        }
        if (this.value.getClass().isArray()) {
            return this.array();
        }
        if (this.value instanceof Annotation nested) {
            return "@" + nested.annotationType().getSimpleName();
        }
        return this.value;
    }
}
