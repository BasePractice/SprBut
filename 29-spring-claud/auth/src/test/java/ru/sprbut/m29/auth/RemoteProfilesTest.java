/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

import java.io.IOException;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.web.client.RestClient;
import ru.sprbut.m29.identity.Identity;

/**
 * Тесты {@link RemoteProfiles}.
 * @since 1.0
 */
@Timeout(10)
final class RemoteProfilesTest {

    @Test
    @DisplayName("клиент передаёт имя профиля в теле запроса")
    void sendsNameInBody() throws IOException {
        try (LiveServer server = new FakeServer(201, path -> "").started()) {
            new RemoteProfiles(RestClient.builder().baseUrl(server.url()).build())
                .create(new Identity(UUID.randomUUID().toString(), "USER", "zina"), "Зинаида");
            MatcherAssert.assertThat(
                "клиент не передал имя профиля",
                server.body("POST", "/profiles"),
                Matchers.containsString("\"name\":\"Зинаида\"")
            );
        }
    }

    @Test
    @DisplayName("клиент обращается к профилям от имени нового пользователя")
    void actsOnBehalfOfNewUser() throws IOException {
        try (LiveServer server = new FakeServer(201, path -> "").started()) {
            final String id = UUID.randomUUID().toString();
            new RemoteProfiles(RestClient.builder().baseUrl(server.url()).build())
                .create(new Identity(id, "USER", "fima"), "Фима");
            MatcherAssert.assertThat(
                "клиент обратился не от имени нового пользователя",
                server.header("POST", "/profiles", "X-User-Id"),
                Matchers.equalTo(id)
            );
        }
    }
}
