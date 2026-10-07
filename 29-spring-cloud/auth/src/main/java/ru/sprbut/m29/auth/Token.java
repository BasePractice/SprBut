/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/**
 * Выпуск access-токена RS256: субъект — идентификатор учётки,
 * в claims — роль, логин и идентификатор сессии {@code sid}.
 * @since 1.0
 */
public final class Token {

    /**
     * Подпись токенов.
     */
    private final JwtEncoder encoder;

    /**
     * Идентификатор ключа подписи.
     */
    private final String kid;

    /**
     * Срок жизни токена.
     */
    private final Duration ttl;

    /**
     * Часы.
     */
    private final Clock clock;

    /**
     * Конструктор.
     * @param encoder Подпись токенов
     * @param kid     Идентификатор ключа подписи
     * @param ttl     Срок жизни токена
     * @param clock   Часы
     */
    public Token(
        final JwtEncoder encoder,
        final String kid,
        final Duration ttl,
        final Clock clock
    ) {
        this.encoder = encoder;
        this.kid = kid;
        this.ttl = ttl;
        this.clock = clock;
    }

    /**
     * Срок жизни токена.
     * @return Секунды
     */
    public long seconds() {
        return this.ttl.toSeconds();
    }

    /**
     * Подписанный токен учётки в сессии.
     * @param account Учётка
     * @param session Идентификатор сессии
     * @return Компактная запись JWT
     */
    public String issued(final Account account, final UUID session) {
        final Instant now = this.clock.instant();
        return this.encoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).keyId(this.kid).build(),
                JwtClaimsSet.builder()
                    .subject(account.id().toString())
                    .id(UUID.randomUUID().toString())
                    .claim("role", account.role().name())
                    .claim("login", account.login())
                    .claim("sid", session.toString())
                    .issuedAt(now)
                    .expiresAt(now.plus(this.ttl))
                    .build()
            )
        ).getTokenValue();
    }
}
