/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.advices;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Совет над телом ответа.
 * @since 1.0
 */
final class ElapsedResponseAdviceTest {

    @Test
    @DisplayName("совет пишет длительность в заголовок до записи тела")
    void writesElapsedHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("sprbut.started", System.nanoTime());
        final ServletServerHttpResponse response =
            new ServletServerHttpResponse(new MockHttpServletResponse());
        new ElapsedResponseAdvice().beforeBodyWrite(
            "тело", null, MediaType.TEXT_PLAIN, StringHttpMessageConverter.class,
            new ServletServerHttpRequest(request), response
        );
        MatcherAssert.assertThat(
            "длительность не попала в заголовок ответа",
            response.getHeaders().getFirst("X-Elapsed-Nanos"),
            Matchers.matchesRegex("\\d+")
        );
    }

    @Test
    @DisplayName("без отметки начала заголовок не пишется")
    void skipsUnstampedRequest() {
        final ServletServerHttpResponse response =
            new ServletServerHttpResponse(new MockHttpServletResponse());
        new ElapsedResponseAdvice().beforeBodyWrite(
            "тело", null, MediaType.TEXT_PLAIN, StringHttpMessageConverter.class,
            new ServletServerHttpRequest(new MockHttpServletRequest()), response
        );
        MatcherAssert.assertThat(
            "длительность посчитана от несуществующей отметки",
            response.getHeaders().getFirst("X-Elapsed-Nanos"),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("тело ответа совет не подменяет")
    void keepsBody() {
        final Object body = new Object();
        MatcherAssert.assertThat(
            "совет подменил тело ответа",
            new ElapsedResponseAdvice().beforeBodyWrite(
                body, null, MediaType.TEXT_PLAIN, StringHttpMessageConverter.class,
                new ServletServerHttpRequest(new MockHttpServletRequest()),
                new ServletServerHttpResponse(new MockHttpServletResponse())
            ),
            Matchers.sameInstance(body)
        );
    }
}
