/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Правило для маркерной аннотации {@link NotBlank}: важен сам факт присутствия.
 * @since 1.0
 */
public final class NotBlankRule implements Rule {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public NotBlankRule() {
        // нечего инициализировать
    }

    @Override
    public List<Violation> check(final Field field, final Object value) {
        final List<Violation> found;
        if (!field.isAnnotationPresent(NotBlank.class)
            || value != null && !String.valueOf(value).isBlank()) {
            found = List.of();
        } else {
            found = List.of(new Violation(field.getName(), "значение обязательно", value));
        }
        return found;
    }
}
