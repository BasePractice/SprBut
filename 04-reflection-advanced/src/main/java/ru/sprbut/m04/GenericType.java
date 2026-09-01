/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 32: «Стирание типов: getGenericType, ParameterizedType».
 *
 * <p>Стирание не абсолютно. Из байткода исчезают типы <i>значений</i>, но
 * <i>объявления</i> — полей, параметров, возвращаемых значений, суперклассов —
 * сохраняют параметры типа в атрибуте {@code Signature}. Именно поэтому Jackson
 * умеет десериализовать в {@code List<Order>}, а Spring — понять, что бину
 * нужен {@code Repository<User>}.</p>
 *
 * <p>{@link Type} — не один класс, а семейство: разбирать сигнатуру приходится,
 * спрашивая у каждого узла, какого он рода.</p>
 *
 * @since 1.0
 */
public final class GenericType {

    /**
     * Тип.
     */
    private final Type type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public GenericType(final Type type) {
        this.type = type;
    }

    /**
     * Параметры типа. Для {@code Map<String, List<Integer>>} — оба, вторым
     * останется вложенный {@code List<Integer>} целиком.
     * @return Параметры типа, каждый целиком
     */
    public List<String> arguments() {
        final List<String> found;
        if (this.type instanceof ParameterizedType parameterized) {
            found = Arrays.stream(parameterized.getActualTypeArguments())
                .map(Type::getTypeName)
                .toList();
        } else {
            found = List.of();
        }
        return found;
    }

    /**
     * Разновидность узла в дереве типов — ключ к разбору произвольной сигнатуры.
     * @return Разновидность узла в дереве типов
     */
    public String kind() {
        final String kind;
        if (this.type instanceof ParameterizedType) {
            kind = "ParameterizedType";
        } else if (this.type instanceof WildcardType) {
            kind = "WildcardType";
        } else if (this.type instanceof TypeVariable<?>) {
            kind = "TypeVariable";
        } else if (this.type instanceof GenericArrayType) {
            kind = "GenericArrayType";
        } else if (this.type instanceof Class<?>) {
            kind = "Class";
        } else {
            kind = this.type.getClass().getSimpleName();
        }
        return kind;
    }

    /**
     * Верхняя граница: для {@code ? extends Number} — {@code Number},
     * для переменной типа — её объявленные границы.
     * @return Верхняя граница типа
     */
    public List<String> bounds() {
        final List<String> found;
        if (this.type instanceof WildcardType wildcard) {
            found = Arrays.stream(wildcard.getUpperBounds()).map(Type::getTypeName).toList();
        } else if (this.type instanceof TypeVariable<?> variable) {
            found = Arrays.stream(variable.getBounds()).map(Type::getTypeName).toList();
        } else {
            found = List.of();
        }
        return found;
    }
}
