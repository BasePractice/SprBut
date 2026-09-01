/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Правило для повторяемого {@link Matches}.
 *
 * <p>Читает через {@code getAnnotationsByType} — единственный способ, работающий
 * и для одного вхождения, и для нескольких. С {@code getAnnotation} второй
 * шаблон потерялся бы молча.</p>
 *
 * @since 1.0
 */
public final class MatchesRule implements Rule {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MatchesRule() {
        // нечего инициализировать
    }

    @Override
    public List<Violation> check(final Field field, final Object value) {
        final Matches[] patterns = field.getAnnotationsByType(Matches.class);
        final List<Violation> found = new ArrayList<>(patterns.length);
        if (patterns.length > 0 && value != null) {
            final String text = String.valueOf(value);
            for (final Matches each : patterns) {
                found.addAll(MatchesRule.mismatch(field, value, each, text));
            }
        }
        return List.copyOf(found);
    }

    private static List<Violation> mismatch(final Field field, final Object value,
        final Matches each, final String text) {
        List<Violation> found;
        try {
            if (Pattern.matches(each.regex(), text)) {
                found = List.of();
            } else {
                found = List.of(
                    new Violation(
                        field.getName(),
                        String.format("%s '%s'", each.message(), each.regex()),
                        value
                    )
                );
            }
        } catch (final PatternSyntaxException malformed) {
            found = List.of(
                new Violation(
                    field.getName(),
                    String.format("некорректный шаблон '%s'", each.regex()),
                    value
                )
            );
        }
        return found;
    }
}
