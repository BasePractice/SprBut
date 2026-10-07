/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Методы для gateway: открытые ключи подписи и активность сессии.
 *
 * <p>Gateway не маршрутизирует их наружу.</p>
 *
 * @since 1.0
 */
@RestController
public final class InternalEndpoint {

    /**
     * Сессии.
     */
    private final Sessions sessions;

    /**
     * Ключ подписи.
     */
    private final RSAKey key;

    /**
     * Конструктор.
     * @param sessions Сессии
     * @param key      Ключ подписи
     */
    public InternalEndpoint(final Sessions sessions, final RSAKey key) {
        this.sessions = sessions;
        this.key = key;
    }

    /**
     * Открытые ключи подписи в формате JWKS.
     * @return JWKS
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(this.key.toPublicJWK()).toJSONObject();
    }

    /**
     * Активна ли сессия.
     * @param id Идентификатор сессии
     * @return Поле {@code active}
     */
    @GetMapping("/internal/sessions/{id}")
    public Map<String, Boolean> session(@PathVariable final UUID id) {
        return Map.of("active", this.sessions.active(id));
    }
}
