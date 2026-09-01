/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Повторяемое ограничение: на одно поле можно навесить несколько шаблонов.
 *
 * <p>Читать его <b>обязательно</b> через {@code getAnnotationsByType}: при двух
 * и более вхождениях в байткоде лежит контейнер {@link MatchesAll},
 * и {@code getAnnotation(Matches.class)} вернёт {@code null}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(MatchesAll.class)
public @interface Matches {

    /**
     * Регулярное выражение, которому значение обязано соответствовать.
     * @return Регулярное выражение, которому значение обязано соответствовать
     */
    String regex();

    /**
     * Сообщение о нарушении.
     * @return Сообщение о нарушении
     */
    String message() default "значение не соответствует шаблону";
}
