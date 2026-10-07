/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.profile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.web.client.RestClient;

/**
 * Тесты {@link RemoteParameters}.
 * @since 1.0
 */
@Timeout(10)
final class RemoteParametersTest {

    @Test
    @DisplayName("клиент разбирает все характеристики пользователя")
    void readsAllParametersOfOwner() throws IOException {
        try (LiveServer server = new FakeServer(200, path -> "{\"eyes\":\"серые\"}").started()) {
            MatcherAssert.assertThat(
                "клиент не разобрал характеристики пользователя",
                new RemoteParameters(RestClient.builder().baseUrl(server.url()).build())
                    .all(UUID.randomUUID()),
                Matchers.equalTo(Map.of("eyes", "серые"))
            );
        }
    }

    @Test
    @DisplayName("отсутствующая характеристика даёт пустое значение")
    void returnsEmptyForMissingParameter() throws IOException {
        try (LiveServer server = new FakeServer(404, path -> "").started()) {
            MatcherAssert.assertThat(
                "клиент выдумал значение отсутствующей характеристики",
                new RemoteParameters(RestClient.builder().baseUrl(server.url()).build())
                    .value(UUID.randomUUID(), "weight"),
                Matchers.equalTo(Optional.empty())
            );
        }
    }

    @Test
    @DisplayName("клиент отправляет значение по пути характеристики")
    void sendsValueToOwnerPath() throws IOException {
        try (LiveServer server = new FakeServer(204, path -> "").started()) {
            final UUID owner = UUID.randomUUID();
            final String value = UUID.randomUUID().toString();
            new RemoteParameters(RestClient.builder().baseUrl(server.url()).build())
                .put(owner, "hobby", value);
            MatcherAssert.assertThat(
                "клиент не отправил значение по пути характеристики",
                server.body("PUT", String.format("/parameters/%s/hobby", owner)),
                Matchers.equalTo(value)
            );
        }
    }

    @Test
    @DisplayName("кириллица в значении доходит до сервиса без искажений")
    void keepsCyrillicValueOnPut() throws IOException {
        try (LiveServer server = new FakeServer(204, path -> "").started()) {
            final UUID owner = UUID.randomUUID();
            new RemoteParameters(RestClient.builder().baseUrl(server.url()).build())
                .put(owner, "eyes", "серо-зелёные");
            MatcherAssert.assertThat(
                "клиент испортил кириллицу по дороге к сервису",
                server.body("PUT", String.format("/parameters/%s/eyes", owner)),
                Matchers.equalTo("серо-зелёные")
            );
        }
    }

    @Test
    @DisplayName("клиент просит сервис удалить характеристики пользователя")
    void deletesParametersOfOwner() throws IOException {
        try (LiveServer server = new FakeServer(204, path -> "").started()) {
            final UUID owner = UUID.randomUUID();
            new RemoteParameters(RestClient.builder().baseUrl(server.url()).build())
                .delete(owner);
            MatcherAssert.assertThat(
                "клиент не попросил сервис удалить характеристики пользователя",
                server.requested("DELETE", String.format("/parameters/%s", owner)),
                Matchers.is(true)
            );
        }
    }
}
