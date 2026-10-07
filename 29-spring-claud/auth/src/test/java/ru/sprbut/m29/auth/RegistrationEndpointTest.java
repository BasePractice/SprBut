/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/**
 * Тесты {@link RegistrationEndpoint}.
 * @since 1.0
 */
final class RegistrationEndpointTest {

    @Test
    @DisplayName("регистрация создаёт профиль с указанным именем")
    void createsProfileWithName() {
        final FakeProfiles profiles = new FakeProfiles();
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "профиль нового пользователя не получил имя",
            profiles.name(
                new RegistrationEndpoint(new FakeAccounts(), profiles).register(
                    new RegistrationEndpoint.Registration("kira", "pwd", name)
                ).getBody().get("id")
            ),
            Matchers.equalTo(name)
        );
    }

    @Test
    @DisplayName("регистрация выдаёт новой учётке роль USER")
    void registersUserWithUserRole() {
        MatcherAssert.assertThat(
            "регистрация выдала роль, отличную от USER",
            new RegistrationEndpoint(new FakeAccounts(), new FakeProfiles()).register(
                new RegistrationEndpoint.Registration(UUID.randomUUID().toString(), "pwd", "Ян")
            ).getBody(),
            Matchers.hasEntry("role", Role.USER)
        );
    }

    @Test
    @DisplayName("сбой сервиса профилей откатывает созданную учётку")
    void undoesAccountWhenProfileFails() {
        final Accounts accounts = new FakeAccounts();
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RegistrationEndpoint(accounts, new BrokenProfiles()).register(
                new RegistrationEndpoint.Registration("ghost", "pwd", "Призрак")
            )
        );
        MatcherAssert.assertThat(
            "учётка без профиля осталась в базе",
            accounts.authenticated("ghost", "pwd").isPresent(),
            Matchers.is(false)
        );
    }
}
