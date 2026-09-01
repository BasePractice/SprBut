/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Многоэлементная аннотация со значениями по умолчанию.
 *
 * <p>Значения по умолчанию — не удобство, а способ добавлять элементы,
 * не ломая уже написанный код.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {

    /**
     * Нижняя граница включительно.
     * @return Нижняя граница включительно
     */
    long min() default 0L;

    /**
     * Верхняя граница включительно.
     * @return Верхняя граница включительно
     */
    long max() default Long.MAX_VALUE;

    /**
     * Сообщение о нарушении.
     * @return Сообщение о нарушении
     */
    String message() default "значение вне допустимого диапазона";
}
