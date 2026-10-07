/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.identity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты {@link Arrived}.
 * @since 1.0
 */
final class ArrivedTest {

    @Test
    @DisplayName("идентификатор читается из заголовков")
    void readsIdentifierFromHeaders() {
        final String id = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "идентификатор из заголовков потерялся",
            new Arrived(
                Map.of("X-User-Id", id, "X-User-Role", "USER", "X-User-Login", "мила")::get
            ).identity().orElseThrow().authentication().getName(),
            Matchers.equalTo(id)
        );
    }

    @Test
    @DisplayName("запрос без заголовка роли не даёт личности")
    void ignoresRequestWithoutRole() {
        MatcherAssert.assertThat(
            "личность собрана без заголовка роли",
            new Arrived(Map.of("X-User-Id", "y", "X-User-Login", "zed")::get).identity(),
            Matchers.equalTo(Optional.empty())
        );
    }
}
