/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.profile;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Тесты {@link Failures}.
 * @since 1.0
 */
@WebMvcTest
@Import({Security.class, SecurityTest.Fakes.class})
final class FailuresTest {

    @Test
    @DisplayName("повторный профиль того же пользователя отвечает 409")
    void dontCreateProfileTwice(@Autowired final MockMvc mvc) throws Exception {
        final UUID id = UUID.randomUUID();
        final String body = String.format(
            "{\"id\":\"%s\",\"login\":\"%s\",\"name\":\"Ян\"}", id, UUID.randomUUID()
        );
        mvc.perform(
            MockMvcRequestBuilders.post("/profiles")
                .header("X-User-Id", id.toString())
                .header("X-User-Role", "USER")
                .header("X-User-Login", "yan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );
        MatcherAssert.assertThat(
            "второй профиль того же пользователя не дал конфликта",
            mvc.perform(
                MockMvcRequestBuilders.post("/profiles")
                    .header("X-User-Id", id.toString())
                    .header("X-User-Role", "USER")
                    .header("X-User-Login", "yan")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            ).andReturn().getResponse().getStatus(),
            Matchers.equalTo(409)
        );
    }
}
