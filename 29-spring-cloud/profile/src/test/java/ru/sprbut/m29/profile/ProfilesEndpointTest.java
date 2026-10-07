/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.sprbut.m29.identity.Identity;

/**
 * Тесты {@link ProfilesEndpoint}.
 * @since 1.0
 */
final class ProfilesEndpointTest {

    @Test
    @DisplayName("пользователь заводит собственный профиль")
    void createsOwnProfile() {
        final UUID id = UUID.randomUUID();
        MatcherAssert.assertThat(
            "пользователь не смог завести собственный профиль",
            new ProfilesEndpoint(new FakeProfiles(), new FakeParameters(), new FakeAccounts())
                .create(
                    new ProfilesEndpoint.Draft(id, "mira", "Мира"),
                    new Identity(id.toString(), "USER", "mira").authentication()
                ).getBody(),
            Matchers.hasEntry("name", "Мира")
        );
    }

    @Test
    @DisplayName("пользователь не может завести профиль другого")
    void dontCreateProfileOfAnotherUser() {
        MatcherAssert.assertThat(
            "пользователь завёл чужой профиль",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new ProfilesEndpoint(
                    new FakeProfiles(), new FakeParameters(), new FakeAccounts()
                ).create(
                    new ProfilesEndpoint.Draft(UUID.randomUUID(), "eva", "Ева"),
                    new Identity(UUID.randomUUID().toString(), "USER", "mal").authentication()
                )
            ).getStatusCode().value(),
            Matchers.equalTo(403)
        );
    }

    @Test
    @DisplayName("переименование меняет имя в профиле")
    void renamesProfile() {
        final Profiles profiles = new FakeProfiles();
        final UUID id = UUID.randomUUID();
        profiles.add(id, "petr", "Пётр");
        final String name = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "переименование не изменило имя",
            new ProfilesEndpoint(profiles, new FakeParameters(), new FakeAccounts()).rename(
                id, new ProfilesEndpoint.Rename(name)
            ),
            Matchers.hasEntry("name", name)
        );
    }

    @Test
    @DisplayName("удаление профиля стирает характеристики пользователя")
    void deletesParametersWithProfile() {
        final Profiles profiles = new FakeProfiles();
        final Parameters parameters = new FakeParameters();
        final UUID id = UUID.randomUUID();
        profiles.add(id, "lida", "Лида");
        parameters.put(id, "eyes", UUID.randomUUID().toString());
        new ProfilesEndpoint(profiles, parameters, new FakeAccounts()).delete(
            id, new Identity(id.toString(), "USER", "lida").authentication()
        );
        MatcherAssert.assertThat(
            "характеристики пережили удалённого пользователя",
            parameters.all(id),
            Matchers.anEmptyMap()
        );
    }

    @Test
    @DisplayName("удаление профиля удаляет учётку в auth")
    void deletesAccountWithProfile() {
        final Profiles profiles = new FakeProfiles();
        final FakeAccounts accounts = new FakeAccounts();
        final UUID id = UUID.randomUUID();
        profiles.add(id, "rem", "Рем");
        new ProfilesEndpoint(profiles, new FakeParameters(), accounts).delete(
            id, new Identity(UUID.randomUUID().toString(), "ADMIN", "root").authentication()
        );
        MatcherAssert.assertThat(
            "учётка в auth пережила свой профиль",
            accounts.gone(id),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("чтение несуществующего профиля отвечает 404")
    void rejectsMissingProfile() {
        MatcherAssert.assertThat(
            "чтение несуществующего профиля не ответило 404",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new ProfilesEndpoint(
                    new FakeProfiles(), new FakeParameters(), new FakeAccounts()
                ).read(UUID.randomUUID())
            ).getStatusCode().value(),
            Matchers.equalTo(404)
        );
    }
}
