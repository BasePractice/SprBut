/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m10.lombok.samples;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * {@code @ToString} и {@code @EqualsAndHashCode} с исключением полей.
 *
 * <p>Частый приём для сущностей: пароль не должен попадать в логи, а равенство
 * определяется идентификатором, а не всем состоянием сразу.</p>
 *
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class Account {

    /**
     * Идентификатор.
     */
    private final String id;

    /**
     * Логин.
     */
    private final String login;

    /**
     * Значение {@code password}.
     */
    private final String password;
}
