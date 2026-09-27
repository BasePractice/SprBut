/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.handlers;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import ru.sprbut.m28.dto.Login;
import ru.sprbut.m28.web.UserController;

/**
 * Резолвер аргумента, помеченного {@code @CurrentUser}.
 * @since 1.0
 */
final class CurrentUserArgumentResolverTest {

    @Test
    @DisplayName("резолвер берётся за параметр с меткой @CurrentUser")
    void supportsMarkedParameter() throws Exception {
        MatcherAssert.assertThat(
            "помеченный параметр остался без резолвера",
            new CurrentUserArgumentResolver().supportsParameter(
                new MethodParameter(UserController.class.getMethod("current", String.class), 0)
            ),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("резолвер не трогает параметр без метки")
    void skipsUnmarkedParameter() throws Exception {
        MatcherAssert.assertThat(
            "резолвер взялся за чужой параметр",
            new CurrentUserArgumentResolver().supportsParameter(
                new MethodParameter(UserController.class.getMethod("greet", Login.class), 0)
            ),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("имя пользователя приходит из заголовка в нижнем регистре")
    void readsHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User", " IVAN ");
        MatcherAssert.assertThat(
            "имя из заголовка не приведено к нижнему регистру",
            new CurrentUserArgumentResolver().resolveArgument(
                null, null, new ServletWebRequest(request), null
            ),
            Matchers.is("ivan")
        );
    }

    @Test
    @DisplayName("запрос без заголовка даёт имя анонима")
    void fallsBackToAnonymous() {
        MatcherAssert.assertThat(
            "запрос без заголовка получил чужое имя",
            new CurrentUserArgumentResolver().resolveArgument(
                null, null, new ServletWebRequest(new MockHttpServletRequest()), null
            ),
            Matchers.is("anonymous")
        );
    }
}
