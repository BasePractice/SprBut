/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * Ключ RS256 из закрытого ключа PKCS#8 в base64; идентификатор ключа — его отпечаток.
 * @since 1.0
 */
public final class SigningKey {

    /**
     * Закрытый ключ PKCS#8 в base64 (DER, без заголовков PEM).
     */
    private final String encoded;

    /**
     * Конструктор.
     * @param encoded Закрытый ключ PKCS#8 в base64 (DER, без заголовков PEM)
     */
    public SigningKey(final String encoded) {
        this.encoded = encoded;
    }

    /**
     * Ключ в виде JWK с закрытой и открытой частями.
     * @return JWK
     */
    public RSAKey jwk() {
        try {
            final KeyFactory factory = KeyFactory.getInstance("RSA");
            final RSAPrivateCrtKey secret = (RSAPrivateCrtKey) factory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(this.encoded))
            );
            final RSAKey key = new RSAKey.Builder(
                (RSAPublicKey) factory.generatePublic(
                    new RSAPublicKeySpec(secret.getModulus(), secret.getPublicExponent())
                )
            ).privateKey(secret).build();
            return new RSAKey.Builder(key).keyID(key.computeThumbprint().toString()).build();
        } catch (final GeneralSecurityException | JOSEException ex) {
            throw new IllegalStateException(
                "Приватный ключ RSA из AUTH_PRIVATE_KEY не читается", ex
            );
        }
    }
}
