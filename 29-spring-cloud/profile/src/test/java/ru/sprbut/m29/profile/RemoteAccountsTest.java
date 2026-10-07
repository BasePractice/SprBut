/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.profile;

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
 * Тесты {@link RemoteAccounts}.
 * @since 1.0
 */
@Timeout(10)
final class RemoteAccountsTest {

    @Test
    @DisplayName("клиент удаляет учётку в auth от имени вызывающего")
    void deletesAccountOnBehalfOfCaller() throws IOException {
        try (LiveServer server = new FakeServer(204, path -> "").started()) {
            final UUID id = UUID.randomUUID();
            new RemoteAccounts(RestClient.builder().baseUrl(server.url()).build())
                .delete(new Identity("admin-id", "ADMIN", "root"), id);
            MatcherAssert.assertThat(
                "клиент не передал в auth роль вызывающего",
                server.header("DELETE", String.format("/auth/users/%s", id), "X-User-Role"),
                Matchers.equalTo("ADMIN")
            );
        }
    }
}
