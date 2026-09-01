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
 * Контейнер для повторяемой {@link Schedule}.
 *
 * <p>Требования языка к контейнеру жёсткие: элемент обязан называться {@code value},
 * иметь тип массива повторяемой аннотации, а retention контейнера — быть не уже,
 * чем у неё самой.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Schedules {

    /**
     * Значение.
     * @return Значение
     */
    Schedule[] value();
}
