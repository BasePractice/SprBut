/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.profile;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/**
 * Тесты {@link CharacteristicsEndpoint}.
 * @since 1.0
 */
final class CharacteristicsEndpointTest {

    @Test
    @DisplayName("сохранённая характеристика читается обратно")
    void returnsStoredCharacteristic() {
        final Profiles profiles = new FakeProfiles();
        final UUID id = UUID.randomUUID();
        profiles.add(id, "kirill", "Кирилл");
        final CharacteristicsEndpoint endpoint =
            new CharacteristicsEndpoint(profiles, new FakeParameters());
        final String value = UUID.randomUUID().toString();
        endpoint.put(id, "height", value);
        MatcherAssert.assertThat(
            "сохранённая характеристика не вернулась",
            endpoint.value(id, "height"),
            Matchers.equalTo(value)
        );
    }

    @Test
    @DisplayName("характеристики несуществующего профиля отвечают 404")
    void rejectsCharacteristicsOfMissingUser() {
        MatcherAssert.assertThat(
            "характеристики несуществующего профиля не ответили 404",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new CharacteristicsEndpoint(new FakeProfiles(), new FakeParameters())
                    .all(UUID.randomUUID())
            ).getStatusCode().value(),
            Matchers.equalTo(404)
        );
    }
}
