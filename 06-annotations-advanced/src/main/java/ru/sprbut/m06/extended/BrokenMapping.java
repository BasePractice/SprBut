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
 * Алиас, указывающий на несуществующий элемент.
 *
 * <p>Компилятор такую ошибку поймать не может: {@link AliasFor} называет элемент
 * строкой. Единственный способ узнать о ней — проверка в момент чтения,
 * и Spring делает ровно это, отказываясь запускаться.</p>
 */
@RequestMapping
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BrokenMapping {

    /**
     * Путь, который некуда положить.
     * @return Путь, который некуда положить
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "нетТакогоЭлемента")
    String value() default "/";
}
