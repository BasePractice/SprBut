/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Композиция второго уровня: цепочки мета-аннотаций бывают и длиннее двух.
 *
 * <p>Именно поэтому поиск обязан быть рекурсивным — проверки «на один уровень
 * вглубь» хватает ровно до первого такого случая.</p>
 */
@RestController
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiController {

    /**
     * Имя бина.
     * @return Имя бина
     */
    String value() default "";
}
