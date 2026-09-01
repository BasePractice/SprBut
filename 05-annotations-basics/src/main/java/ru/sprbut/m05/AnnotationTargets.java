/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 39: {@code @Target{TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE}}.
 *
 * <p>{@code @Target} — <b>ограничение компилятора</b>, а не рантайма. Он не даст
 * поставить аннотацию туда, где её быть не должно; в runtime проверять уже нечего,
 * потому что неправильный код просто не собрался бы.</p>
 *
 * <p>Отдельная тонкость: <b>отсутствие</b> {@code @Target} означает «можно почти
 * везде», а не «нигде».</p>
 *
 * @since 1.0
 */
public final class AnnotationTargets {

    /**
     * Аннотация.
     */
    private final Class<? extends Annotation> annotation;

    /**
     * Основной конструктор.
     * @param annotation Аннотация
     */
    public AnnotationTargets(final Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Куда разрешено ставить аннотацию — читается из её собственной мета-аннотации.
     * @return Куда разрешено ставить аннотацию — читается из её собственной мета-аннотации
     */
    public List<ElementType> allowed() {
        final Target target = this.annotation.getAnnotation(Target.class);
        if (target == null) {
            return List.of();
        }
        return Arrays.asList(target.value());
    }

    /**
     * Можно ли поставить аннотацию на поле.
     * @return Можно ли поставить аннотацию на поле
     */
    public boolean fields() {
        return this.allowed().contains(ElementType.FIELD);
    }

    /**
     * Можно ли поставить аннотацию на класс.
     * @return Можно ли поставить аннотацию на класс
     */
    public boolean types() {
        return this.allowed().contains(ElementType.TYPE);
    }
}
