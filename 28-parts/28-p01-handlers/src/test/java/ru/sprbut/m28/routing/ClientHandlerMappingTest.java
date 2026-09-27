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
import ru.sprbut.m28.web.DeviceController;

/**
 * Таблица маршрутов, которая знает про {@code @Client}.
 * @since 1.0
 */
final class ClientHandlerMappingTest {

    @Test
    @DisplayName("метод с меткой получает своё условие")
    void conditionsMarkedMethod() throws Exception {
        MatcherAssert.assertThat(
            "метка на методе не превратилась в условие",
            new ClientHandlerMapping().getCustomMethodCondition(
                DeviceController.class.getMethod("mobile")
            ),
            Matchers.instanceOf(ClientCondition.class)
        );
    }

    @Test
    @DisplayName("метод без метки остаётся без условия")
    void skipsUnmarkedMethod() throws Exception {
        MatcherAssert.assertThat(
            "метод без метки получил условие",
            new ClientHandlerMapping().getCustomMethodCondition(
                DeviceController.class.getMethod("common")
            ),
            Matchers.nullValue()
        );
    }
}
