/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты {@link SigningKey}.
 * @since 1.0
 */
final class SigningKeyTest {

    @Test
    @DisplayName("публичный ключ восстанавливается из приватного")
    void restoresPublicKeyFromPrivate() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair pair = generator.generateKeyPair();
        MatcherAssert.assertThat(
            "публичный ключ не восстановился из приватного",
            new SigningKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()))
                .jwk().toRSAPublicKey(),
            Matchers.equalTo(pair.getPublic())
        );
    }

    @Test
    @DisplayName("ключ получает идентификатор по отпечатку")
    void namesKeyByThumbprint() throws Exception {
        MatcherAssert.assertThat(
            "идентификатор ключа не задан",
            new FreshKey().jwk().getKeyID(),
            Matchers.not(Matchers.emptyOrNullString())
        );
    }

    @Test
    @DisplayName("мусор вместо ключа отвергается исключением")
    void rejectsGarbage() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new SigningKey("bm90LWEta2V5").jwk()
        );
    }
}
