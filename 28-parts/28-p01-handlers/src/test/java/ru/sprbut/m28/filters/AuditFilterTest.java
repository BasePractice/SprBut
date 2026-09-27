/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.filters;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Фильтр, записывающий тело запроса в журнал.
 * @since 1.0
 */
final class AuditFilterTest {

    @Test
    @DisplayName("тело, прочитанное дальше по цепочке, попадает в журнал")
    void journalsReadBody() throws Exception {
        final List<String> journal = new ArrayList<>(1);
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContent("{\"username\":\"fedor\"}".getBytes(StandardCharsets.UTF_8));
        new AuditFilter(journal::add, 1024).doFilter(
            request, new MockHttpServletResponse(),
            (req, res) -> req.getInputStream().readAllBytes()
        );
        MatcherAssert.assertThat(
            "прочитанное тело не попало в журнал",
            journal,
            Matchers.contains("тело запроса POST /api/users: {\"username\":\"fedor\"}")
        );
    }

    @Test
    @DisplayName("тело, которое никто не прочитал, журнал не видит")
    void missesUnreadBody() throws Exception {
        final List<String> journal = new ArrayList<>(1);
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        request.setContent("{\"username\":\"fedor\"}".getBytes(StandardCharsets.UTF_8));
        new AuditFilter(journal::add, 1024).doFilter(
            request, new MockHttpServletResponse(), (req, res) -> req.getContentType()
        );
        MatcherAssert.assertThat(
            "кэширующая обёртка прочитала тело сама",
            journal,
            Matchers.contains("тело запроса POST /api/users: ")
        );
    }

    @Test
    @DisplayName("без журнала фильтр не собирается")
    void rejectsMissingJournal() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new AuditFilter(null, 1024),
            "фильтр собран без журнала"
        );
    }
}
