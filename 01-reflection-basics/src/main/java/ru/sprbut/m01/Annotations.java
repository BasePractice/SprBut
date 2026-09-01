/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 9: «Получать аннотации».
 *
 * <p>{@link AnnotatedElement} — общий интерфейс для {@code Class}, {@code Field},
 * {@code Method} и {@code Parameter}, поэтому один и тот же код читает аннотации
 * с любого элемента.</p>
 *
 * <p>Видны только аннотации с {@code RetentionPolicy.RUNTIME}. Остальных здесь нет
 * не потому, что их не нашли, а потому, что в загруженном классе их физически
 * не существует.</p>
 *
 * @since 1.0
 */
public final class Annotations {

    /**
     * Элемент.
     */
    private final AnnotatedElement element;

    /**
     * Основной конструктор.
     * @param element Элемент
     */
    public Annotations(final AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Присутствует ли аннотация на элементе.
     * @param type Тип
     * @return Присутствует ли аннотация на элементе
     */
    public boolean has(final Class<? extends Annotation> type) {
        return this.element.isAnnotationPresent(type);
    }

    /**
     * Аннотация, если она есть.
     * @param type Тип
     * @return Аннотация, если она есть
     */
    public <A extends Annotation> Optional<A> find(final Class<A> type) {
        return Optional.ofNullable(this.element.getAnnotation(type));
    }

    /**
     * Имена всех runtime-аннотаций элемента в алфавитном порядке.
     * @return Имена всех runtime-аннотаций элемента в алфавитном порядке
     */
    public List<String> names() {
        return Arrays.stream(this.element.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .sorted()
            .toList();
    }
}
