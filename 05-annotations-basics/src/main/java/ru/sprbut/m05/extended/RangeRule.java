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
        if (range == null || value == null) {
            return List.of();
        }
        if (!(value instanceof Number number)) {
            return List.of(new Violation(
                field.getName(),
                "@Range применим только к числам, а поле имеет тип "
                    + field.getType().getSimpleName(),
                value
            ));
        }
        if (number.longValue() < range.min() || number.longValue() > range.max()) {
            return List.of(new Violation(
                field.getName(),
                range.message() + " [" + range.min() + ", " + range.max() + "]",
                value
            ));
        }
        return List.of();
    }
}
