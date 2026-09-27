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
 * Пятеро участников, увиденные через ответ приложения.
 * @since 1.0
 */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@WebMvcTest(UserController.class)
@Import(HandlersConfig.class)
@DisplayName("Участники обработки запроса, увиденные через ответ")
final class UserControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("конвертер приводит логин к нижнему регистру до вызова метода")
    void convertsLoginToLowerCase() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/users/greet").param("name", "IVAN"))
            .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("ivan")));
    }

    @Test
    @DisplayName("запрос без заголовка X-User отвечает именем анонима")
    void callsUnnamedVisitorAnonymous() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/users/me"))
            .andExpect(MockMvcResultMatchers.content().string("anonymous"));
    }

    @Test
    @DisplayName("резолвер собирает аргумент из заголовка, а не из строки запроса")
    void readsCurrentUserFromHeader() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/users/me").header("X-User", " IVAN "))
            .andExpect(MockMvcResultMatchers.content().string("ivan"));
    }

    @Test
    @DisplayName("интерсептор дописывает в ответ длительность обработки")
    void measuresHandlingTime() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/users/me")
        ).andExpect(
            MockMvcResultMatchers.header().string(
                "X-Elapsed-Nanos", Matchers.matchesRegex("\\d+")
            )
        );
    }

    @Test
    @DisplayName("совет маскирует почту до того, как тело дойдёт до метода")
    void masksEmailBeforeHandler() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ivan\",\"email\":\"ivan@example.com\"}")
        ).andExpect(
            MockMvcResultMatchers.jsonPath("$.email", Matchers.is("i***@example.com"))
        );
    }

    @Test
    @DisplayName("фильтр вырезает скрипт из тела запроса")
    void cutsScriptFromBody() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"<script>x</script>ivan\",\"email\":\"a@b.ru\"}")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.is("ivan")));
    }

    @Test
    @DisplayName("пустое имя не проходит проверку и отвечает кодом 400")
    void rejectsBlankUsername() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"  \",\"email\":\"ivan@example.com\"}")
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
