/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.errors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Обработчик неподдержанных операций.
 * @since 1.0
 */
final class UnsupportedResolverTest {

    @Test
    @DisplayName("неподдержанная операция отвечает кодом 501")
    void answersNotImplemented() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        new UnsupportedResolver().resolveException(
            new MockHttpServletRequest(), response, null,
            new UnsupportedOperationException("архивирование")
        );
        MatcherAssert.assertThat(
            "неподдержанная операция ответила не кодом 501",
            response.getStatus(),
            Matchers.is(501)
        );
    }

    @Test
    @DisplayName("чужое исключение передаётся следующему обработчику")
    void passesForeignError() {
        MatcherAssert.assertThat(
            "обработчик взялся за чужое исключение",
            new UnsupportedResolver().resolveException(
                new MockHttpServletRequest(), new MockHttpServletResponse(), null,
                new IllegalStateException("сломалось")
            ),
            Matchers.nullValue()
        );
    }
}
