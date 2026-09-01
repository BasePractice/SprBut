/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * Найденная аннотация вместе со слитыми значениями элементов.
 *
 * <p>Путь мета-аннотаций хранится не для отладки, а по делу: когда значение
 * пришло не оттуда, откуда ожидалось, единственный способ понять почему —
 * увидеть, через какую цепочку его нашли.</p>
 *
 * @param type       тип найденной аннотации
 * @param attributes слитые значения элементов
 * @param path       цепочка мета-аннотаций от элемента до цели
 * @since 1.0
 */
public record Merged(
    Class<? extends Annotation> type,
    Map<String, Object> attributes,
    List<String> path
) {

    /**
     * Значение {@code Merged}.
     */
    public Merged {
        attributes = Map.copyOf(attributes);
        path = List.copyOf(path);
    }

    /**
     * Значение элемента.
     */
    public Object value(final String attribute) {
        return this.attributes.get(attribute);
    }

    /**
     * Значение элемента строкой.
     */
    public String text(final String attribute) {
        return String.valueOf(this.attributes.get(attribute));
    }
}
