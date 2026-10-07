/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import org.springframework.security.oauth2.jwt.Jwt;
import ru.sprbut.m29.identity.Identity;

/**
 * Пользователь, предъявивший проверенный access-токен.
 * @since 1.0
 */
public final class Bearer {

    /**
     * Проверенный токен.
     */
    private final Jwt jwt;

    /**
     * Конструктор.
     * @param jwt Проверенный токен
     */
    public Bearer(final Jwt jwt) {
        this.jwt = jwt;
    }

    /**
     * Пользователь из claims {@code sub}, {@code role} и {@code login}.
     * @return Пользователь
     */
    public Identity identity() {
        return new Identity(
            this.jwt.getSubject(),
            this.jwt.getClaimAsString("role"),
            this.jwt.getClaimAsString("login")
        );
    }
}
