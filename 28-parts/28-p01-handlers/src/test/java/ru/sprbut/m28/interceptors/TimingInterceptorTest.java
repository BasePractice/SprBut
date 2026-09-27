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
    @DisplayName("preHandle ставит в запрос отметку начала обработки")
    void stampsRequest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        new TimingInterceptor().preHandle(request, new MockHttpServletResponse(), new Object());
        MatcherAssert.assertThat(
            "отметка начала обработки не попала в запрос",
            request.getAttribute("sprbut.started"),
            Matchers.instanceOf(Long.class)
        );
    }

    @Test
    @DisplayName("повторная диспетчеризация не сдвигает отметку начала")
    void keepsFirstStamp() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("sprbut.started", 42L);
        new TimingInterceptor().preHandle(request, new MockHttpServletResponse(), new Object());
        MatcherAssert.assertThat(
            "вторая диспетчеризация перезаписала отметку начала",
            request.getAttribute("sprbut.started"),
            Matchers.is(42L)
        );
    }
}
