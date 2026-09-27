/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.routing;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Подмена таблицы маршрутов.
 * @since 1.0
 */
final class ClientRegistrationsTest {

    @Test
    @DisplayName("Boot получает таблицу маршрутов, которая знает про @Client")
    void suppliesOwnMapping() {
        MatcherAssert.assertThat(
            "таблица маршрутов не подменена",
            new ClientRegistrations().getRequestMappingHandlerMapping(),
            Matchers.instanceOf(ClientHandlerMapping.class)
        );
    }
}
