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
 * Слайд 55: «{@code @RestController} = {@code @Controller} + {@code @ResponseBody}».
 *
 * <p>Композиция мета-аннотаций — приём, на котором держится вся декларативная
 * часть Spring. Никакого «наследования аннотаций» в языке нет: это просто
 * аннотация, на которую навешены две другие.</p>
 */
@Controller
@ResponseBody
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {

    /**
     * Имя бина.
     * @return Имя бина
     */
    String value() default "";
}
