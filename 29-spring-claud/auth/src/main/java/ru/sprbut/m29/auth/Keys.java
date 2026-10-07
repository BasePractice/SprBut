/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Ключ RS256 из окружения и выпуск access-токенов.
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public final class Keys {

    /**
     * Конструктор для Spring.
     */
    private Keys() {
    }

    /**
     * Ключ подписи из окружения.
     * @param encoded Закрытый ключ PKCS#8 в base64
     * @return Ключ
     */
    @Bean
    static RSAKey key(@Value("${jwt.private-key}") final String encoded) {
        return new SigningKey(encoded).jwk();
    }

    /**
     * Подпись токенов.
     * @param key Ключ подписи
     * @return Подпись
     */
    @Bean
    static JwtEncoder encoder(final RSAKey key) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    /**
     * Выпуск access-токенов.
     * @param encoder Подпись токенов
     * @param key     Ключ подписи
     * @param ttl     Срок жизни access-токена
     * @return Выпуск токенов
     */
    @Bean
    static Token token(
        final JwtEncoder encoder,
        final RSAKey key,
        @Value("${jwt.access-ttl:PT1H}") final Duration ttl
    ) {
        return new Token(encoder, key.getKeyID(), ttl, Clock.systemUTC());
    }
}
