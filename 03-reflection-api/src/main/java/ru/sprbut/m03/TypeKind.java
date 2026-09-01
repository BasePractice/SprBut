/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

/**
 * Категория типа одним словом.
 *
 * <p>Порядок проверок здесь — не стилистика, а необходимость: аннотация является
 * интерфейсом, enum является классом. Перепутанный порядок молча выдаст
 * правдоподобный, но неверный ответ.</p>
 *
 * @since 1.0
 */
public final class TypeKind {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public TypeKind(final Class<?> type) {
        this.type = type;
    }

    /**
     * Одно из: primitive, array, enum, annotation, interface, record, class.
     * @return Одно из: primitive, array, enum, annotation, interface, record, class
     */
    public String name() {
        final String kind;
        if (this.type.isPrimitive()) {
            kind = "primitive";
        } else if (this.type.isArray()) {
            kind = "array";
        } else if (this.type.isEnum()) {
            kind = "enum";
        } else if (this.type.isAnnotation()) {
            kind = "annotation";
        } else if (this.type.isInterface()) {
            kind = "interface";
        } else if (this.type.isRecord()) {
            kind = "record";
        } else {
            kind = "class";
        }
        return kind;
    }
}
