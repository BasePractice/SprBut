/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.filters;

import jakarta.servlet.DispatcherType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Фильтр, срабатывающий один раз на запрос.
 * @since 1.0
 */
final class CorrelationFilterTest {

    @Test
    @DisplayName("идентификатор из заголовка запроса уходит в ответ")
    void echoesHeader() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "req-7f3");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        new CorrelationFilter().doFilter(request, response, new MockFilterChain());
        MatcherAssert.assertThat(
            "идентификатор запроса не дошёл до ответа",
            response.getHeader("X-Correlation-Id"),
            Matchers.is("req-7f3")
        );
    }

    @Test
    @DisplayName("запрос без идентификатора получает новый")
    void generatesMissingId() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        new CorrelationFilter().doFilter(
            new MockHttpServletRequest(), response, new MockFilterChain()
        );
        MatcherAssert.assertThat(
            "запрос без идентификатора остался без него",
            response.getHeader("X-Correlation-Id"),
            Matchers.matchesRegex("[0-9a-f-]{36}")
        );
    }

    @Test
    @DisplayName("идентификатор кладётся в атрибут запроса для следующих участников")
    void exposesAttribute() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "req-c20");
        new CorrelationFilter().doFilter(
            request, new MockHttpServletResponse(), new MockFilterChain()
        );
        MatcherAssert.assertThat(
            "идентификатор не попал в атрибут запроса",
            request.getAttribute("sprbut.correlation"),
            Matchers.is("req-c20")
        );
    }

    @Test
    @DisplayName("на повторной диспетчеризации ошибки фильтр не срабатывает")
    void skipsErrorDispatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setDispatcherType(DispatcherType.ERROR);
        request.setAttribute("jakarta.servlet.error.request_uri", "/api/users");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        new CorrelationFilter().doFilter(request, response, new MockFilterChain());
        MatcherAssert.assertThat(
            "фильтр сработал второй раз на странице ошибки",
            response.getHeader("X-Correlation-Id"),
            Matchers.nullValue()
        );
    }
}
