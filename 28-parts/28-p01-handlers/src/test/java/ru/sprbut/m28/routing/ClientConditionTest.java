/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.routing;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Условие выбора обработчика по виду клиента.
 * @since 1.0
 */
final class ClientConditionTest {

    @Test
    @DisplayName("условие подходит запросу со своим видом клиента в любом регистре")
    void matchesOwnClient() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client", "Watch");
        final ClientCondition condition = new ClientCondition("watch");
        MatcherAssert.assertThat(
            "условие не узнало свой вид клиента",
            condition.getMatchingCondition(request),
            Matchers.sameInstance(condition)
        );
    }

    @Test
    @DisplayName("условие не подходит запросу без заголовка")
    void rejectsUnknownClient() {
        MatcherAssert.assertThat(
            "условие подошло запросу без вида клиента",
            new ClientCondition("watch").getMatchingCondition(new MockHttpServletRequest()),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("без вида клиента условие не собирается")
    void rejectsMissingKind() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new ClientCondition(null),
            "условие собрано без вида клиента"
        );
    }
}
