/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.Field;

/**
 * Слайд 7: статическое поле читается без экземпляра.
 *
 * <p>Единственное отличие от {@link ObjectField} — {@code get(null)}: у статического
 * поля нет владельца, и передавать рефлексии некого.</p>
 *
 * @since 1.0
 */
public final class StaticField {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Имя.
     */
    private final String name;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param name Имя
     */
    public StaticField(final Class<?> type, final String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Значение статического поля.
     * @return Значение статического поля
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object value() {
        final Field field = new Declared(this.type).field(this.name);
        field.setAccessible(true);
        try {
            return field.get(null);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Не удалось прочитать статическое поле %s", this.name, denied)
            );
        }
    }
}
