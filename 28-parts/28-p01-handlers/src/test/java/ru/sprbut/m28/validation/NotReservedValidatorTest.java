/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.validation;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Исполнитель правила о зарезервированных именах.
 * @since 1.0
 */
final class NotReservedValidatorTest {

    @Test
    @DisplayName("зарезервированное имя отвергается в любом регистре")
    void rejectsReservedName() {
        MatcherAssert.assertThat(
            "зарезервированное имя прошло проверку",
            new NotReservedValidator().isValid(" Root ", null),
            Matchers.is(false)
        );
    }

    @Test
    @DisplayName("обычное имя проходит проверку")
    void acceptsOrdinaryName() {
        MatcherAssert.assertThat(
            "обычное имя отвергнуто",
            new NotReservedValidator().isValid("fedor", null),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("пустое значение оставлено другим правилам")
    void leavesNullToOthers() {
        MatcherAssert.assertThat(
            "правило взялось проверять пустое значение",
            new NotReservedValidator().isValid(null, null),
            Matchers.is(true)
        );
    }
}
