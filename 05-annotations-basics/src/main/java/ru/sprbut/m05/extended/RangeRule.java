/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Правило для {@link Range}.
 *
 * <p>Аннотация на поле неподходящего типа — тоже нарушение, и молчать о нём нельзя:
 * компилятор такую ошибку не поймает, {@code @Target(FIELD)} разрешает любое поле.</p>
 *
 * @since 1.0
 */
public final class RangeRule implements Rule {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public RangeRule() {
        // нечего инициализировать
    }

    @Override
    public List<Violation> check(final Field field, final Object value) {
        final Range range = field.getAnnotation(Range.class);
        final List<Violation> found;
        if (range == null || value == null) {
            found = List.of();
        } else if (value instanceof Number number) {
            found = RangeRule.outside(field, number, range);
        } else {
            found = List.of(
                new Violation(
                    field.getName(),
                    String.format(
                        "@Range применим только к числам, а поле имеет тип %s",
                        field.getType().getSimpleName()
                    ),
                    value
                )
            );
        }
        return found;
    }

    private static List<Violation> outside(final Field field, final Number number,
        final Range range) {
        final List<Violation> found;
        if (number.longValue() < range.min() || number.longValue() > range.max()) {
            found = List.of(
                new Violation(
                    field.getName(),
                    String.format("%s [%s, %s]", range.message(), range.min(), range.max()),
                    number
                )
            );
        } else {
            found = List.of();
        }
        return found;
    }
}
