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
 * Выбор метода по своему условию.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("Выбор метода по своему условию")
final class DeviceControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("запрос без вида клиента попадает в общий метод")
    void servesCommonClient() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/device"))
            .andExpect(MockMvcResultMatchers.content().string("обычный клиент"));
    }

    @Test
    @DisplayName("мобильный клиент попадает в свой метод на том же пути")
    void servesMobileClient() throws Exception {
        this.http.perform(MockMvcRequestBuilders.get("/api/device").header("X-Client", "mobile"))
            .andExpect(MockMvcResultMatchers.content().string("мобильный клиент"));
    }
}
