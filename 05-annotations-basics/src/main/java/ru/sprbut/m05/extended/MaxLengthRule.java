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
        if (limit == null || value == null) {
            return List.of();
        }
        final int length = String.valueOf(value).length();
        if (
            length > limit.value()
        ) {
            return List.of(new Violation(
                field.getName(),
                "длина " + length + " превышает максимум " + limit.value(),
                value
            ));
        }
        return List.of();
    }
}
