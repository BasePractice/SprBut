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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.sprbut.m28.config.HandlersConfig;

/**
 * Ответ об отвергнутом теле запроса.
 * @since 1.0
 */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@WebMvcTest(UserController.class)
@Import(HandlersConfig.class)
@DisplayName("Ответ об отвергнутом теле запроса")
final class FailuresTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("ответ об ошибке приходит форматом RFC 9457, а не обычным JSON")
    void answersWithProblemDetail() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"email\":\"почта\"}")
        ).andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            )
        );
    }

    @Test
    @DisplayName("каждое нарушенное правило занимает свой пункт перечня")
    void listsEveryBrokenRule() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"email\":\"почта\"}")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.errors", Matchers.hasSize(2)));
    }

    @Test
    @DisplayName("пункт перечня называет поле, не прошедшее проверку")
    void namesBrokenField() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"email\":\"почта\"}")
        ).andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.errors[0]", Matchers.startsWith("email:")
            )
        );
    }

    @Test
    @DisplayName("пункт перечня объясняет, чем именно поле не устроило")
    void explainsWhatWentWrong() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"email\":\"ivan@example.com\"}")
        ).andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.errors[0]", Matchers.containsString("короче трёх")
            )
        );
    }

    @Test
    @DisplayName("имя со скобками не проходит проверку по образцу")
    void rejectsStrangeUsername() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ivan(1)\",\"email\":\"ivan@example.com\"}")
        ).andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.errors[0]", Matchers.containsString("кроме букв")
            )
        );
    }

    @Test
    @DisplayName("зарезервированное имя отвергается своим правилом проверки")
    void rejectsReservedUsername() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"System\",\"email\":\"ivan@example.com\"}")
        ).andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.errors[0]", Matchers.containsString("зарезервировано")
            )
        );
    }

    @Test
    @DisplayName("неразборчивый JSON тоже отвечает в формате RFC 9457")
    void answersBrokenJsonWithProblem() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":")
        ).andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(
                MediaType.APPLICATION_PROBLEM_JSON
            )
        );
    }
}
