/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * Аннотации, которые видны на элементе в runtime.
 *
 * <p>Важно именно слово «видны»: это не то, что написано в исходниках. Аннотации
 * с политикой {@code SOURCE} и {@code CLASS} сюда не попадут — не потому,
 * что их отфильтровали, а потому что в загруженном классе их нет.</p>
 *
 * @since 1.0
 */
public final class VisibleAnnotations {

    /**
     * Элемент.
     */
    private final AnnotatedElement element;

    /**
     * Основной конструктор.
     * @param element Элемент
     */
    public VisibleAnnotations(final AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Имена видимых аннотаций в алфавитном порядке.
     * @return Имена видимых аннотаций в алфавитном порядке
     */
    public List<String> names() {
        return Arrays.stream(this.element.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .sorted()
            .toList();
    }
}
