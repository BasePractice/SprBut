/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ограничение, которое никогда не сработает: retention равен {@code CLASS}.
 *
 * <p>Аннотация есть в исходниках, есть в байткоде — и полностью невидима
 * рефлексии. Это ровно та ошибка, которую делают, забыв
 * {@code @Retention(RUNTIME)}, и она не вызывает ни предупреждения,
 * ни ошибки: просто ничего не происходит.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface InvisibleNotNull {
}
