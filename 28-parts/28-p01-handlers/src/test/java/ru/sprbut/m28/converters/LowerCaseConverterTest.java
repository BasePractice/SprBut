/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.converters;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m28.dto.Login;

/**
 * Конвертер строки запроса в логин.
 * @since 1.0
 */
final class LowerCaseConverterTest {

    @Test
    @DisplayName("логин приходит в нижнем регистре и без пробелов по краям")
    void trimsAndLowersLogin() {
        MatcherAssert.assertThat(
            "логин не приведён к нижнему регистру",
            new LowerCaseConverter().convert("  IvAn  "),
            Matchers.is(new Login("ivan"))
        );
    }

    @Test
    @DisplayName("уже нормальный логин конвертер не портит")
    void keepsNormalLogin() {
        MatcherAssert.assertThat(
            "нормальный логин изменился",
            new LowerCaseConverter().convert("ivan"),
            Matchers.is(new Login("ivan"))
        );
    }
}
