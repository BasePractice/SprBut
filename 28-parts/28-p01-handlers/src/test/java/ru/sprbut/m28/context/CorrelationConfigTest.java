/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.context;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Бин области запроса.
 * @since 1.0
 */
final class CorrelationConfigTest {

    @Test
    @DisplayName("бин берёт идентификатор из атрибута текущего запроса")
    void readsCurrentRequest() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("sprbut.correlation", "req-5d1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        final String id;
        try {
            id = new CorrelationConfig().correlation().id();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        MatcherAssert.assertThat(
            "бин не нашёл идентификатор текущего запроса",
            id,
            Matchers.is("req-5d1")
        );
    }

    @Test
    @DisplayName("вне запроса бин собрать нельзя")
    void cannotBuildOutsideRequest() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new CorrelationConfig().correlation(),
            "бин области запроса собран без запроса"
        );
    }

    @Test
    @DisplayName("запрос без атрибута идентификатора отвергается, а не получает «null»")
    void rejectsMissingAttribute() {
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest())
        );
        try {
            Assertions.assertThrows(
                NullPointerException.class,
                () -> new CorrelationConfig().correlation(),
                "запрос без идентификатора получил идентификатор"
            );
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
