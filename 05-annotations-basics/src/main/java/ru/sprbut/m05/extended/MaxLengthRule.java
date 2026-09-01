/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Правило для {@link MaxLength}: значение элемента аннотации становится
 * параметром проверки.
 * @since 1.0
 */
public final class MaxLengthRule implements Rule {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MaxLengthRule() {
        // нечего инициализировать
    }

    @Override
    public List<Violation> check(final Field field, final Object value) {
        final MaxLength limit = field.getAnnotation(MaxLength.class);
        final List<Violation> found;
        if (limit == null || value == null) {
            found = List.of();
        } else {
            found = MaxLengthRule.oversized(field, value, limit);
        }
        return found;
    }

    private static List<Violation> oversized(final Field field, final Object value,
        final MaxLength limit) {
        final int length = String.valueOf(value).length();
        final List<Violation> found;
        if (length > limit.value()) {
            found = List.of(
                new Violation(
                    field.getName(),
                    String.format("длина %s превышает максимум %s", length, limit.value()),
                    value
                )
            );
        } else {
            found = List.of();
        }
        return found;
    }
}
