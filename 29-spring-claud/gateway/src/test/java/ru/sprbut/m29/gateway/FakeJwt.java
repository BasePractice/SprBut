/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Проверенный access-токен для тестов.
 * @since 1.0
 */
final class FakeJwt {

    /**
     * Идентификатор пользователя.
     */
    private final String subject;

    /**
     * Роль.
     */
    private final String role;

    /**
     * Идентификатор сессии.
     */
    private final String sid;

    /**
     * Конструктор.
     * @param subject Идентификатор пользователя
     * @param role    Роль
     * @param sid     Идентификатор сессии
     */
    FakeJwt(final String subject, final String role, final String sid) {
        this.subject = subject;
        this.role = role;
        this.sid = sid;
    }

    /**
     * Токен.
     * @return Токен
     */
    Jwt jwt() {
        return Jwt.withTokenValue(UUID.randomUUID().toString())
            .header("alg", "RS256")
            .subject(this.subject)
            .jti(UUID.randomUUID().toString())
            .claim("role", this.role)
            .claim("login", String.format("login-%s", this.subject))
            .claim("sid", this.sid)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    }
}
