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
        if (this.type.isPrimitive()) {
            return "primitive";
        }
        if (this.type.isArray()) {
            return "array";
        }
        if (this.type.isEnum()) {
            return "enum";
        }
        if (this.type.isAnnotation()) {
            return "annotation";
        }
        if (this.type.isInterface()) {
            return "interface";
        }
        if (this.type.isRecord()) {
            return "record";
        }
        return "class";
    }
}
