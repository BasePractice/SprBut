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
 * Данные, переданные фильтром дальше по запросу.
 * @since 1.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@DisplayName("Данные, переданные фильтром дальше по запросу")
final class TraceControllerTest {

    /**
     * Значение {@code http}.
     */
    @Autowired
    private MockMvc http;

    @Test
    @DisplayName("метод получает идентификатор через @RequestAttribute")
    void readsAttribute() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/trace/attribute").header("X-Correlation-Id", "req-a1")
        ).andExpect(MockMvcResultMatchers.content().string("req-a1"));
    }

    @Test
    @DisplayName("бин области запроса отвечает значением текущего запроса")
    void readsScopedBean() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/trace/bean").header("X-Correlation-Id", "req-b2")
        ).andExpect(MockMvcResultMatchers.content().string("req-b2"));
    }

    @Test
    @DisplayName("бин области запроса доступен и из рабочего потока")
    void readsScopedBeanAsync() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.asyncDispatch(
                this.http.perform(
                    MockMvcRequestBuilders.get("/api/trace/async/bean")
                        .header("X-Correlation-Id", "req-c3")
                ).andReturn()
            )
        ).andExpect(MockMvcResultMatchers.content().string("req-c3"));
    }

    @Test
    @DisplayName("MDC журнала доходит до рабочего потока")
    void readsLogContextAsync() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.asyncDispatch(
                this.http.perform(
                    MockMvcRequestBuilders.get("/api/trace/async/log")
                        .header("X-Correlation-Id", "req-e5")
                ).andReturn()
            )
        ).andExpect(MockMvcResultMatchers.content().string("req-e5"));
    }

    @Test
    @DisplayName("идентификатор запроса возвращается клиенту в заголовке")
    void echoesHeader() throws Exception {
        this.http.perform(
            MockMvcRequestBuilders.get("/api/trace/bean").header("X-Correlation-Id", "req-d4")
        ).andExpect(MockMvcResultMatchers.header().string("X-Correlation-Id", "req-d4"));
    }
}
