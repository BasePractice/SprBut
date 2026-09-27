/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.config;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.ModelAndView;

/**
 * Фильтры, зарегистрированные вручную.
 * @since 1.0
 */
final class FiltersConfigTest {

    @Test
    @DisplayName("журнал тел стоит только на путях API")
    void auditsOnlyApi() {
        MatcherAssert.assertThat(
            "журнал тел зарегистрирован не на те пути",
            new FiltersConfig().audit().getUrlPatterns(),
            Matchers.contains("/api/*")
        );
    }

    @Test
    @DisplayName("ограничение длины тела стоит в цепочке первым")
    void limitsFirst() {
        MatcherAssert.assertThat(
            "ограничение длины тела стоит не первым",
            new FiltersConfig().limit((req, res, handler, error) -> new ModelAndView()).getOrder(),
            Matchers.is(Ordered.HIGHEST_PRECEDENCE)
        );
    }

    @Test
    @DisplayName("ETag считается только для приветствия")
    void tagsOnlyGreeting() {
        MatcherAssert.assertThat(
            "ETag зарегистрирован не на те пути",
            new FiltersConfig().etag().getUrlPatterns(),
            Matchers.contains("/api/greeting")
        );
    }
}
