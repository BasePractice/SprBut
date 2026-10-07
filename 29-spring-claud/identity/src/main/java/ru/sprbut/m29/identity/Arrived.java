/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.identity;

import java.util.Optional;
import java.util.function.Function;

/**
 * Пользователь, пришедший в заголовках запроса.
 * @since 1.0
 */
public final class Arrived {

    /**
     * Значение заголовка по имени, {@code null} при отсутствии.
     */
    private final Function<String, String> headers;

    /**
     * Конструктор.
     * @param headers Значение заголовка по имени, {@code null} при отсутствии
     */
    public Arrived(final Function<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Пользователь, если пришли все три заголовка.
     * @return Пользователь
     */
    public Optional<Identity> identity() {
        return Optional.ofNullable(this.headers.apply(Header.ID.toString())).flatMap(
            id -> Optional.ofNullable(this.headers.apply(Header.ROLE.toString())).flatMap(
                role -> Optional.ofNullable(this.headers.apply(Header.LOGIN.toString())).map(
                    login -> new Identity(id, role, login)
                )
            )
        );
    }
}
