/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.identity;

import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Проверенный gateway пользователь: идентификатор, роль и логин.
 *
 * <p>Имя аутентификации — идентификатор, роль — полномочие с приставкой
 * {@code ROLE_}, логин — в деталях.</p>
 *
 * @since 1.0
 */
public final class Identity {

    /**
     * Идентификатор.
     */
    private final String uid;

    /**
     * Роль.
     */
    private final String role;

    /**
     * Логин.
     */
    private final String login;

    /**
     * Конструктор.
     * @param uid   Идентификатор
     * @param role  Роль
     * @param login Логин
     */
    public Identity(final String uid, final String role, final String login) {
        this.uid = uid;
        this.role = role;
        this.login = login;
    }

    /**
     * Аутентификация Spring Security.
     * @return Аутентификация
     */
    public Authentication authentication() {
        final UsernamePasswordAuthenticationToken auth =
            UsernamePasswordAuthenticationToken.authenticated(
                this.uid,
                null,
                List.of(new SimpleGrantedAuthority(String.format("ROLE_%s", this.role)))
            );
        auth.setDetails(this.login);
        return auth;
    }

    /**
     * Заголовки для запроса к другому сервису от имени пользователя.
     * @return Значения по именам заголовков
     */
    public Map<String, String> headers() {
        return Map.of(
            Header.ID.toString(), this.uid,
            Header.ROLE.toString(), this.role,
            Header.LOGIN.toString(), this.login
        );
    }

    /**
     * Представление для ответа клиенту.
     * @return Идентификатор, роль и логин по именам
     */
    public Map<String, Object> json() {
        return Map.of("id", this.uid, "role", this.role, "login", this.login);
    }
}
