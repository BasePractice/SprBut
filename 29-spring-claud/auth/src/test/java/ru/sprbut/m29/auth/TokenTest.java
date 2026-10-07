/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.time.Duration;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты {@link Token}.
 * @since 1.0
 */
final class TokenTest {

    @Test
    @DisplayName("токен несёт логин учётки")
    void carriesLogin() throws Exception {
        final FreshKey key = new FreshKey();
        final String login = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "токен потерял логин",
            key.decoder().decode(
                key.token(Duration.ofMinutes(2)).issued(
                    new FakeAccounts().add(login, "pwd"), UUID.randomUUID()
                )
            ).getClaimAsString("login"),
            Matchers.equalTo(login)
        );
    }

    @Test
    @DisplayName("токен несёт роль учётки")
    void carriesRole() throws Exception {
        final FreshKey key = new FreshKey();
        final Account account = new FakeAccounts().add("boss", "pwd");
        account.grant(Role.ADMIN);
        MatcherAssert.assertThat(
            "токен потерял роль",
            key.decoder().decode(
                key.token(Duration.ofMinutes(2)).issued(account, UUID.randomUUID())
            ).getClaimAsString("role"),
            Matchers.equalTo("ADMIN")
        );
    }
}
