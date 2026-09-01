/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.targets;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Аннотации на использовании типа поля.
 *
 * <p>Ключевая мысль слайда 48 в одном факте: {@code field.getAnnotations()} этих
 * аннотаций <b>не видит вовсе</b>. Они лежат на {@link AnnotatedType}, а внутри
 * дженерика — ещё этажом глубже, на аргументах типа.</p>
 *
 * @since 1.0
 */
public final class TypeUse {

    /**
     * Поле.
     */
    private final Field field;

    /**
     * Основной конструктор.
     * @param field Поле
     */
    public TypeUse(final Field field) {
        this.field = field;
    }

    /**
     * Аннотации на самом типе поля.
     * @return Аннотации на самом типе поля
     */
    public List<String> onType() {
        return this.names(this.field.getAnnotatedType());
    }

    /**
     * Аннотации на аргументах дженерика: {@code List<@NonNull String>}.
     * @return Аннотации на аргументах дженерика: {@code List<@NonNull String>}
     */
    public List<String> onArguments() {
        if (this.field.getAnnotatedType() instanceof AnnotatedParameterizedType parameterized) {
            return Arrays.stream(parameterized.getAnnotatedActualTypeArguments())
                .flatMap(argument -> this.names(argument).stream())
                .toList();
        }
        return List.of();
    }

    private List<String> names(final AnnotatedType type) {
        return Arrays.stream(type.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .toList();
    }
}
