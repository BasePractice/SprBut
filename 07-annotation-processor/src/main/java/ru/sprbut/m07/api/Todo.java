/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m07.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 60: «Анализ исходного кода».
 *
 * <p>Процессор может вообще ничего не генерировать — только проверять код
 * и писать диагностику через {@code Messager}. Так работают Error Prone,
 * NullAway и проверки {@code @Nullable}.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Todo {

    /**
     * Значение.
     * @return Значение
     */
    String value();

    /**
     * {@code true} — сборка падает с ошибкой, {@code false} — только предупреждение.
     * @return {@code true} — сборка падает с ошибкой, {@code false} — только предупреждение
     */
    boolean blocking() default false;
}
