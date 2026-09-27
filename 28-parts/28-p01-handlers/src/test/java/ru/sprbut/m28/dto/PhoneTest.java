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
 * Телефон.
 * @since 1.0
 */
final class PhoneTest {

    @Test
    @DisplayName("телефон без цифр не собирается")
    void rejectsNull() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new Phone(null),
            "телефон собран без цифр"
        );
    }
}
