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
 * Связывание формы, настроенное контроллером.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("Связывание формы, настроенное контроллером")
final class ProfileControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("анкета с ролью, назначенной клиентом, отвергается")
    void rejectsSelfAssignedRole() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/profiles")
                .param("name", "Нина")
                .param("city", "Томск")
                .param("role", "ADMIN")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("отказ называет роль и причину")
    void explainsRejectedRole() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/profiles")
                .param("name", "Нина")
                .param("role", "ADMIN")
        ).andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.errors[0]", Matchers.is("role: роль назначает сервер")
            )
        );
    }

    @Test
    @DisplayName("имя приходит без пробелов по краям")
    void trimsName() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/profiles")
                .param("name", "  Нина ")
                .param("city", "Томск")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("Нина")));
    }

    @Test
    @DisplayName("город из одних пробелов становится пустым")
    void blanksWhitespaceCity() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/profiles")
                .param("name", "Нина")
                .param("city", "   ")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.city", Matchers.nullValue()));
    }

    @Test
    @DisplayName("регион собран методом @ModelAttribute из заголовка")
    void readsRegionHeader() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/profiles")
                .header("X-Region", " kz ")
                .param("name", "Нина")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.region", Matchers.is("KZ")));
    }

    @Test
    @DisplayName("без заголовка регион берётся по умолчанию")
    void defaultsRegion() throws Exception {
        this.http.perform(MockMvcRequestBuilders.post("/api/profiles").param("name", "Нина"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.region", Matchers.is("RU")));
    }
}
