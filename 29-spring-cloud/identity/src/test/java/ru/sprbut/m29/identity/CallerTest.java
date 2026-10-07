/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.identity;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты {@link Caller}.
 * @since 1.0
 */
final class CallerTest {

    @Test
    @DisplayName("вызывающий восстанавливает личность из аутентификации")
    void restoresIdentityFromAuthentication() {
        final String login = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "вызывающий не восстановил личность, из которой собран",
            new Caller(new Identity("id-7", "ADMIN", login).authentication()).identity().json(),
            Matchers.equalTo(new Identity("id-7", "ADMIN", login).json())
        );
    }

    @Test
    @DisplayName("пользователь владеет самим собой")
    void ownsItself() {
        MatcherAssert.assertThat(
            "пользователь не владеет самим собой",
            new Caller(new Identity("me", "USER", "self").authentication()).owns("me"),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("пользователь не владеет чужой учёткой")
    void dontOwnAnotherUser() {
        MatcherAssert.assertThat(
            "пользователь владеет чужой учёткой",
            new Caller(new Identity("me", "USER", "self").authentication()).owns("you"),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("администратор владеет любым пользователем")
    void letsAdminOwnAnybody() {
        MatcherAssert.assertThat(
            "администратор не владеет другим пользователем",
            new Caller(new Identity("root", "ADMIN", "boss").authentication())
                .owns(UUID.randomUUID().toString()),
            Matchers.is(true)
        );
    }
}
