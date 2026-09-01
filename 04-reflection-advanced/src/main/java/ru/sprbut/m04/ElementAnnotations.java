/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 36: {@link AnnotatedElement} — единый разбор аннотаций.
 *
 * <p>Класс, поле, метод, конструктор и параметр реализуют один интерфейс,
 * поэтому код чтения аннотаций пишется ровно один раз. Это и позволяет
 * Spring поддерживать {@code @Qualifier} одинаково во всех этих местах.</p>
 *
 * @since 1.0
 */
public final class ElementAnnotations {

    /**
     * Элемент.
     */
    private final AnnotatedElement element;

    /**
     * Основной конструктор.
     * @param element Элемент
     */
    public ElementAnnotations(final AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Имена runtime-аннотаций элемента в алфавитном порядке.
     * @return Имена runtime-аннотаций элемента в алфавитном порядке
     */
    public List<String> names() {
        return Arrays.stream(this.element.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .sorted()
            .toList();
    }
}
