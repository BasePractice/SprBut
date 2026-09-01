/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Элементы аннотации со значениями конкретного использования — без слияния.
 *
 * <p>Основа, поверх которой накладываются переопределения композитных аннотаций.</p>
 *
 * @since 1.0
 */
public final class RawAttributes {

    /**
     * Аннотация.
     */
    private final Annotation annotation;

    /**
     * Основной конструктор.
     * @param annotation Аннотация
     */
    public RawAttributes(final Annotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Значения всех элементов.
     * @return Значения всех элементов
     */
    public Map<String, Object> map() {
        final Map<String, Object> collected = new LinkedHashMap<>();
        for (final Method element : this.annotation.annotationType().getDeclaredMethods()) {
            collected.put(element.getName(), this.value(element));
        }
        return collected;
    }

    /**
     * Значение одного элемента.
     * @param element Элемент
     * @return Значение одного элемента
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object value(final Method element) {
        try {
            element.setAccessible(true);
            return element.invoke(this.annotation);
        } catch (final ReflectiveOperationException denied) {
            throw new IllegalStateException("Не прочитать элемент " + element.getName(), denied);
        }
    }
}
