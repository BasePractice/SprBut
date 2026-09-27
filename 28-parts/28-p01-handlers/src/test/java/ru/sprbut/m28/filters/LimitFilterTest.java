/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.filters;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * Фильтр, отвергающий слишком длинные тела.
 * @since 1.0
 */
final class LimitFilterTest {

    @Test
    @DisplayName("длинное тело не проходит дальше по цепочке")
    void stopsLongBody() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContent(new byte[65]);
        final MockFilterChain chain = new MockFilterChain();
        new LimitFilter((req, res, handler, error) -> new ModelAndView(), 64).doFilter(
            request, new MockHttpServletResponse(), chain
        );
        MatcherAssert.assertThat(
            "длинное тело прошло дальше по цепочке",
            chain.getRequest(),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("отказ передаётся обработчику исключений диспетчера")
    void delegatesRejection() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContent(new byte[65]);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        new LimitFilter(
            (req, res, handler, error) -> {
                res.setStatus(418);
                return new ModelAndView();
            },
            64
        ).doFilter(request, response, new MockFilterChain());
        MatcherAssert.assertThat(
            "отказ не дошёл до обработчика исключений",
            response.getStatus(),
            Matchers.is(418)
        );
    }

    @Test
    @DisplayName("тело в пределах нормы проходит дальше")
    void passesShortBody() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContent(new byte[64]);
        final MockFilterChain chain = new MockFilterChain();
        new LimitFilter((req, res, handler, error) -> new ModelAndView(), 64).doFilter(
            request, new MockHttpServletResponse(), chain
        );
        MatcherAssert.assertThat(
            "короткое тело остановлено фильтром",
            chain.getRequest(),
            Matchers.sameInstance(request)
        );
    }

    @Test
    @DisplayName("без обработчика исключений фильтр не собирается")
    void rejectsMissingResolver() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new LimitFilter(null, 64),
            "фильтр собран без обработчика исключений"
        );
    }
}
