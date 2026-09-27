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
 * Свой обработчик результата и @ResponseBody.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("Свой обработчик результата и @ResponseBody")
final class PlainControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("в обычном контроллере строки уходят построчным текстом")
    void writesLines() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/plain/users")).andExpect(
            MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder("ivan", "petr"))
        );
    }

    @Test
    @DisplayName("с @ResponseBody тот же результат перехватывает Jackson")
    void losesToResponseBody() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/plain/users/json"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[1]", Matchers.is("petr")));
    }
}
