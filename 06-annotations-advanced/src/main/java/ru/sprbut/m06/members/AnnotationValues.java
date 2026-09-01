/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.members;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Фактические значения элементов конкретного использования аннотации.
 *
 * <p>Незаданные элементы возвращают свои умолчания — отличить одно от другого
 * можно только сравнив с {@link AnnotationMembers#defaults()}. Ровно так
 * устроен отчёт {@code /actuator/configprops}.</p>
 *
 * @since 1.0
 */
public final class AnnotationValues {

    /**
     * Аннотация.
     */
    private final Annotation annotation;

    /**
     * Основной конструктор.
     * @param annotation Аннотация
     */
    public AnnotationValues(final Annotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Значения всех элементов.
     * @return Значения всех элементов
     */
    public Map<String, Object> values() {
        final Map<String, Object> collected = new LinkedHashMap<>();
        for (final Method element : this.annotation.annotationType().getDeclaredMethods()) {
            collected.put(element.getName(), new Described(this.read(element)).text());
        }
        return Map.copyOf(collected);
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private Object read(final Method element) {
        try {
            element.setAccessible(true);
            return element.invoke(this.annotation);
        } catch (final ReflectiveOperationException denied) {
            throw new IllegalStateException("Не прочитать элемент " + element.getName(), denied);
        }
    }
}
