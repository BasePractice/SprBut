/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Логин.
 * @since 1.0
 */
final class LoginTest {

    @Test
    @DisplayName("логин без значения не собирается")
    void rejectsNull() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new Login(null),
            "логин собран без значения"
        );
    }
}
