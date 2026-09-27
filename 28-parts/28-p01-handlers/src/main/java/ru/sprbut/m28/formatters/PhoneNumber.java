/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.formatters;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Метка параметра, который разбирается как телефон.
 *
 * <p>В отличие от {@code @CurrentUser}, у метки есть атрибут, и
 * {@link PhoneFormatterFactory} собирает под него свой форматтер. Один
 * и тот же тип {@code Phone} в разных методах может разбираться
 * по-разному — так же устроены {@code @DateTimeFormat} и
 * {@code @NumberFormat}.</p>
 *
 * @since 1.0
 */
@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {

    /**
     * Код страны, который подставляется вместо ведущей восьмёрки.
     * @return Код страны
     */
    String country() default "7";
}
