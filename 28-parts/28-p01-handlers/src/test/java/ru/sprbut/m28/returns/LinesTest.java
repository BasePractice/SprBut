/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.returns;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Ответ построчно.
 * @since 1.0
 */
final class LinesTest {

    @Test
    @DisplayName("ответ без строк не собирается")
    void rejectsNull() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new Lines(null),
            "ответ собран без строк"
        );
    }
}
