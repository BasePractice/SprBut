/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Идентификатор одного запроса.
 * @since 1.0
 */
final class RequestCorrelationTest {

    @Test
    @DisplayName("идентификатор без значения не собирается")
    void rejectsNull() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new RequestCorrelation(null),
            "идентификатор собран без значения"
        );
    }
}
