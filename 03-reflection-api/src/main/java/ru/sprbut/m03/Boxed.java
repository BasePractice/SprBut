/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

/**
 * Тип с примитивом, заменённым на его обёртку.
 *
 * <p>Рефлексия объявляет параметр как {@code int}, а аргументом всегда приходит
 * {@code Integer}: в массиве {@code Object[]} примитивов не бывает. Без этой
 * замены любая проверка совместимости типов вернула бы ложь.</p>
 *
 * @since 1.0
 */
public final class Boxed {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Boxed(final Class<?> type) {
        this.type = type;
    }

    /**
     * Тип-обёртка для примитива; для ссылочного типа — он сам.
     * @return Тип-обёртка для примитива; для ссылочного типа — он сам
     * @checkstyle CyclomaticComplexityCheck (20 lines)
     */
    public Class<?> type() {
        final Class<?> boxed;
        if (this.type.isPrimitive()) {
            boxed = switch (this.type.getName()) {
                case "int" -> Integer.class;
                case "long" -> Long.class;
                case "double" -> Double.class;
                case "float" -> Float.class;
                case "short" -> Short.class;
                case "byte" -> Byte.class;
                case "char" -> Character.class;
                case "boolean" -> Boolean.class;
                case null, default -> Void.class;
            };
        } else {
            boxed = this.type;
        }
        return boxed;
    }
}
