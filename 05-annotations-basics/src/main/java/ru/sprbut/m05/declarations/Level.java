/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.declarations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 43: «Single (обычно value), указывать имя параметра не обязательно
 * {@code @SuppressWarnings("")}».
 *
 * <p>Если у аннотации ровно один элемент и он назван {@code value}, при использовании
 * имя можно опустить: {@code @Level("INFO")} вместо {@code @Level(value = "INFO")}.
 * Это соглашение языка, а не магия — именно поэтому элемент почти всегда зовут
 * {@code value}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Level {

    /**
     * Значение.
     * @return Значение
     */
    String value();
}
