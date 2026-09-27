/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.interceptors;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Перенос MDC в рабочий поток.
 * @since 1.0
 */
final class MdcPropagationTest {

    @Test
    @DisplayName("MDC потока запроса восстанавливается в рабочем потоке")
    void restoresContext() throws Exception {
        final ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());
        final MdcPropagation propagation = new MdcPropagation();
        MDC.setContextMap(Map.of("correlation", "req-9e4"));
        propagation.beforeConcurrentHandling(request, () -> "задача");
        MDC.clear();
        propagation.preProcess(request, () -> "задача");
        final String restored = MDC.get("correlation");
        MDC.clear();
        MatcherAssert.assertThat(
            "MDC не дошёл до рабочего потока",
            restored,
            Matchers.is("req-9e4")
        );
    }

    @Test
    @DisplayName("после задачи MDC рабочего потока очищается")
    void clearsContext() throws Exception {
        MDC.put("correlation", "req-0b7");
        new MdcPropagation().postProcess(
            new ServletWebRequest(new MockHttpServletRequest()), () -> "задача", "итог"
        );
        MatcherAssert.assertThat(
            "MDC остался в потоке после задачи",
            MDC.get("correlation"),
            Matchers.nullValue()
        );
    }
}
