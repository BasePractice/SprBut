/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.interceptors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Интерсептор диспетчера.
 * @since 1.0
 */
final class TimingInterceptorTest {

    @Test
    @DisplayName("preHandle пропускает запрос дальше, к обработчику")
    void letsRequestThrough() {
        MatcherAssert.assertThat(
            "запрос не пропущен к обработчику",
            new TimingInterceptor().preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()
            ),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("postHandle дописывает в ответ длительность обработки")
    void writesElapsedHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final TimingInterceptor interceptor = new TimingInterceptor();
        interceptor.preHandle(request, response, new Object());
        interceptor.postHandle(request, response, new Object(), null);
        MatcherAssert.assertThat(
            "длительность обработки не попала в ответ",
            response.getHeader("X-Elapsed-Nanos"),
            Matchers.matchesRegex("\\d+")
        );
    }
}
