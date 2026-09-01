/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Отдать заранее заданный результат, не вызывая цель вовсе.
 *
 * <p>Крайний случай аспекта: поведение метода полностью заменяется метаданными.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Stubbed {

    /**
     * Значение, которое вернёт метод.
     * @return Значение, которое вернёт метод
     */
    String value();
}
