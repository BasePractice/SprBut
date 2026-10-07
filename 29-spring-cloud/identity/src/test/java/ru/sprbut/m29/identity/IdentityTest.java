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
 * Тесты {@link Identity}.
 * @since 1.0
 */
final class IdentityTest {

    @Test
    @DisplayName("имя аутентификации равно идентификатору пользователя")
    void takesIdentifierAsAuthenticationName() {
        final String id = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "имя аутентификации не совпало с идентификатором пользователя",
            new Identity(id, "USER", "мила").authentication().getName(),
            Matchers.equalTo(id)
        );
    }

    @Test
    @DisplayName("роль превращается в полномочие ROLE_")
    void turnsRoleIntoAuthority() {
        MatcherAssert.assertThat(
            "роль не превратилась в полномочие ROLE_",
            new Identity("x", "ADMIN", "boss").authentication()
                .getAuthorities().iterator().next().getAuthority(),
            Matchers.equalTo("ROLE_ADMIN")
        );
    }

    @Test
    @DisplayName("личность передаёт логин в исходящих заголовках")
    void passesItselfInHeaders() {
        final String login = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "личность потеряла логин в исходящих заголовках",
            new Identity("id", "USER", login).headers(),
            Matchers.hasEntry("X-User-Login", login)
        );
    }
}
