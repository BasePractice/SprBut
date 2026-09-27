/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.handlers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Метка аргумента, за которым стоит резолвер.
 *
 * <p>Сама по себе аннотация не делает ничего — как и любая другая
 * (модуль 05). Значение появляется оттого, что её читает
 * {@link CurrentUserArgumentResolver}, а его вызывает диспетчер.</p>
 *
 * @since 1.0
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
