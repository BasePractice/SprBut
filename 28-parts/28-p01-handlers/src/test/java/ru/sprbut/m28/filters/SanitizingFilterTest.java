/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.filters;

import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Фильтр сервлет-контейнера.
 * @since 1.0
 */
final class SanitizingFilterTest {

    @Test
    @DisplayName("скрипт остаётся в теле, дошедшем до следующего звена цепочки")
    void cutsScriptFromJsonBody() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(
            "{\"username\":\"<script>alert(1)</script>ivan\"}".getBytes(StandardCharsets.UTF_8)
        );
        final MockFilterChain chain = new MockFilterChain();
        new SanitizingFilter().doFilter(request, new MockHttpServletResponse(), chain);
        MatcherAssert.assertThat(
            "скрипт не вырезан из тела запроса",
            new String(
                chain.getRequest().getInputStream().readAllBytes(), StandardCharsets.UTF_8
            ),
            Matchers.is("{\"username\":\"ivan\"}")
        );
    }

    @Test
    @DisplayName("запрос без тела JSON уходит дальше нетронутым")
    void passesFormRequestUntouched() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        final MockFilterChain chain = new MockFilterChain();
        new SanitizingFilter().doFilter(request, new MockHttpServletResponse(), chain);
        MatcherAssert.assertThat(
            "запрос без JSON зачем-то обёрнут",
            chain.getRequest(),
            Matchers.sameInstance(request)
        );
    }

    @Test
    @DisplayName("тело читается повторно, потому что обёртка держит его в памяти")
    void allowsSecondRead() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"username\":\"ivan\"}".getBytes(StandardCharsets.UTF_8));
        final MockFilterChain chain = new MockFilterChain();
        new SanitizingFilter().doFilter(request, new MockHttpServletResponse(), chain);
        chain.getRequest().getInputStream().readAllBytes();
        MatcherAssert.assertThat(
            "второе чтение тела вернуло пустой поток",
            chain.getRequest().getReader().readLine(),
            Matchers.is("{\"username\":\"ivan\"}")
        );
    }
}
