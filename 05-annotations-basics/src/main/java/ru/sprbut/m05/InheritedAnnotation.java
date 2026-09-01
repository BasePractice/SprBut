/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Слайд 41: {@code @Inherited} и три границы, за которые оно не работает.
 *
 * <p>Наследуется только аннотация <b>класса</b>, только от суперкласса и только
 * если у неё самой есть {@code @Inherited}. Метод не наследует аннотацию
 * никогда, интерфейс — тоже никогда.</p>
 *
 * <p>Именно из-за этих ограничений Spring не полагается на {@code @Inherited},
 * а обходит иерархию сам ({@code AnnotatedElementUtils}). Как выглядит такой
 * обход — в {@link HierarchySearch} и в модуле 06.</p>
 *
 * @param <A> Тип аннотации
 * @since 1.0
 */
public final class InheritedAnnotation<A extends Annotation> {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Аннотация.
     */
    private final Class<A> annotation;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param annotation Аннотация
     */
    public InheritedAnnotation(final Class<?> type, final Class<A> annotation) {
        this.type = type;
        this.annotation = annotation;
    }

    /**
     * Аннотация с учётом {@code @Inherited}.
     * @return Аннотация с учётом {@code @Inherited}
     */
    public Optional<A> found() {
        return Optional.ofNullable(this.type.getAnnotation(this.annotation));
    }

    /**
     * Аннотация, объявленная на самом классе: наследование игнорируется.
     * @return Аннотация, объявленная на самом классе: наследование игнорируется
     */
    public Optional<A> declared() {
        return Optional.ofNullable(this.type.getDeclaredAnnotation(this.annotation));
    }
}
