/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.targets;

import java.util.Arrays;
import java.util.List;

/**
 * Аннотации на объявлениях переменных типа: {@code class Holder<@Comparablish T>}.
 *
 * <p>Ещё одна ветка API, отдельная и от полей, и от типов их использования.</p>
 *
 * @since 1.0
 */
public final class TypeParameters {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public TypeParameters(final Class<?> type) {
        this.type = type;
    }

    /**
     * Имена аннотаций на переменной типа с указанным номером.
     * @param index Индекс
     * @return Имена аннотаций на переменной типа с указанным номером
     */
    public List<String> names(final int index) {
        return Arrays.stream(this.type.getTypeParameters()[index].getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .toList();
    }
}
