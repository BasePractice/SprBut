/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Пользователь, от имени которого пришёл запрос, восстановленный из аутентификации.
 *
 * <p>Нужен, чтобы позвать другой сервис от имени того же пользователя.</p>
 *
 * @since 1.0
 */
public final class Caller {

    /**
     * Аутентификация, построенная {@link HeaderAuthentication}.
     */
    private final Authentication auth;

    /**
     * Конструктор.
     * @param auth Аутентификация, построенная {@link HeaderAuthentication}
     */
    public Caller(final Authentication auth) {
        this.auth = auth;
    }

    /**
     * Пользователь.
     * @return Пользователь
     */
    public Identity identity() {
        return new Identity(
            this.auth.getName(),
            this.auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(
                    () -> new IllegalStateException(
                        String.format("У вызывающего %s нет роли", this.auth.getName())
                    )
                ),
            String.valueOf(this.auth.getDetails())
        );
    }

    /**
     * Является ли пользователь администратором или указанным пользователем.
     * @param uid Идентификатор пользователя
     * @return Истина, если это он сам или администратор
     */
    public boolean owns(final String uid) {
        return this.auth.getName().equals(uid) || this.auth.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
