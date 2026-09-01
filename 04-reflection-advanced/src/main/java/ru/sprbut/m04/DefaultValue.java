/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Значение по умолчанию для типа.
 *
 * <p>Для ссылочного типа это {@code null}, для примитива — его собственный ноль.
 * Вернуть {@code null} вместо {@code int} нельзя: прокси упал бы на распаковке,
 * причём в месте, никак не связанном с настоящей причиной.</p>
 *
 * @since 1.0
 */
public final class DefaultValue {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public DefaultValue(final Class<?> type) {
        this.type = type;
    }

    /**
     * Значение по умолчанию.
     * @return Значение по умолчанию
     */
    public Object value() {
        final Object value;
        if (!this.type.isPrimitive() || this.type == void.class) {
            value = null;
        } else if (this.type == boolean.class) {
            value = false;
        } else if (this.type == char.class) {
            value = '\0';
        } else if (this.type == long.class) {
            value = 0L;
        } else if (this.type == double.class) {
            value = 0.0d;
        } else if (this.type == float.class) {
            value = 0.0f;
        } else {
            value = 0;
        }
        return value;
    }
}
