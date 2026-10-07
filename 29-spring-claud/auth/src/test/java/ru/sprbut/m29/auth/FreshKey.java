/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Свежий ключ RSA для тестов, прочитанный через {@link SigningKey}.
 * @since 1.0
 */
final class FreshKey {

    /**
     * Ключ.
     */
    private final RSAKey key;

    /**
     * Конструктор нового случайного ключа.
     * @throws NoSuchAlgorithmException Если в JVM нет RSA
     */
    FreshKey() throws NoSuchAlgorithmException {
        this(FreshKey.generated());
    }

    /**
     * Конструктор.
     * @param key Ключ
     */
    private FreshKey(final RSAKey key) {
        this.key = key;
    }

    /**
     * Ключ в виде JWK.
     * @return JWK
     */
    RSAKey jwk() {
        return this.key;
    }

    /**
     * Выпуск токенов, подписанных этим ключом.
     * @param ttl Срок жизни токена
     * @return Выпуск токенов
     */
    Token token(final Duration ttl) {
        return new Token(
            new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(this.key))),
            this.key.getKeyID(),
            ttl,
            Clock.systemUTC()
        );
    }

    /**
     * Проверка токенов открытой частью ключа.
     * @return Проверка
     * @throws JOSEException Если ключ не RSA
     */
    JwtDecoder decoder() throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(this.key.toRSAPublicKey()).build();
    }

    private static RSAKey generated() throws NoSuchAlgorithmException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return new SigningKey(
            Base64.getEncoder().encodeToString(
                generator.generateKeyPair().getPrivate().getEncoded()
            )
        ).jwk();
    }
}
