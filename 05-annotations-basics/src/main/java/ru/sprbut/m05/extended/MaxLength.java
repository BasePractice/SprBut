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
 * Single-value аннотация: элемент назван {@code value}, поэтому имя
 * при использовании можно опустить — {@code @MaxLength(10)}.
 *
 * <p>Ровно то соглашение, о котором говорит слайд 42.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MaxLength {

    /**
     * Максимальная длина значения.
     * @return Максимальная длина значения
     */
    int value();
}
