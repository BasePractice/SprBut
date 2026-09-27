/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Контроллер под аспектом.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("Контроллер под аспектом")
final class SearchControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("аспект обрезает все строковые аргументы до вызова метода")
    void trimsEveryArgument() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/search")
                .param("query", "  чайники ")
                .param("city", " Омск  ")
        ).andExpect(MockMvcResultMatchers.content().string("[чайники] в [Омск]"));
    }
}
