/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.web;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Версии API и ETag.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("Версии API и ETag")
final class GreetingControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("запрос без версии попадает в версию по умолчанию")
    void servesDefaultVersion() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/greeting"))
            .andExpect(MockMvcResultMatchers.content().string("Привет"));
    }

    @Test
    @DisplayName("версия из заголовка выбирает другой метод на том же пути")
    void servesSecondVersion() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/greeting").header("X-API-Version", "2"))
            .andExpect(MockMvcResultMatchers.content().string("Здравствуйте"));
    }

    @Test
    @DisplayName("версия, которую никто не обслуживает, отвечает кодом 400")
    void rejectsUnknownVersion() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/greeting").header("X-API-Version", "7"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("ответ с тем же ETag заменяется кодом 304")
    void answersNotModified() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/greeting").header(
                "If-None-Match",
                this.http.perform(MockMvcRequestBuilders.get("/api/greeting"))
                    .andReturn().getResponse().getHeader("ETag")
            )
        ).andExpect(MockMvcResultMatchers.status().isNotModified());
    }

    @Test
    @DisplayName("ответ несёт ETag, посчитанный по телу")
    void tagsResponse() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/greeting"))
            .andExpect(MockMvcResultMatchers.header().string("ETag", Matchers.startsWith("\"")));
    }
}
