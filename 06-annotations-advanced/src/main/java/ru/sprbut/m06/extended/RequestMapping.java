/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m06.extended;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Базовая аннотация маршрута — учебный аналог {@code @RequestMapping}.
 *
 * <p>Все остальные аннотации этого пакета в конечном счёте сводятся к ней,
 * и именно её ищет сканер, раскрывая композиции.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {

    /**
     * Путь маршрута.
     * @return Путь маршрута
     */
    String path() default "";

    /**
     * Метод HTTP.
     * @return Метод HTTP
     */
    HttpMethod method() default HttpMethod.GET;

    /**
     * Типы содержимого ответа.
     * @return Типы содержимого ответа
     */
    String[] produces() default {};
}
