/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

/**
 * Проверка токена: подпись и срок — исходной проверкой, затем активность
 * его сессии в auth.
 * @since 1.0
 */
public final class ActiveTokens implements ReactiveJwtDecoder {

    /**
     * Проверка подписи и срока.
     */
    private final ReactiveJwtDecoder origin;

    /**
     * Сессии.
     */
    private final Sessions sessions;

    /**
     * Конструктор.
     * @param origin   Проверка подписи и срока
     * @param sessions Сессии
     */
    public ActiveTokens(final ReactiveJwtDecoder origin, final Sessions sessions) {
        this.origin = origin;
        this.sessions = sessions;
    }

    @Override
    public Mono<Jwt> decode(final String token) {
        return this.origin.decode(token).flatMap(
            jwt -> Mono.justOrEmpty(jwt.getClaimAsString("sid"))
                .switchIfEmpty(
                    Mono.error(new BadJwtException("В токене нет признака сессии sid"))
                )
                .flatMap(this.sessions::active)
                .filter(Boolean::booleanValue)
                .map(active -> jwt)
                .switchIfEmpty(
                    Mono.error(
                        new BadJwtException(
                            String.format("Сессия токена %s закрыта", jwt.getId())
                        )
                    )
                )
        );
    }
}
