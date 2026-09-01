/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m01;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

/**
 * Слайд 4: Class.forName — загрузка класса по строке.
 * @since 1.0
 */
@DisplayName("Слайд 4: Class.forName — загрузка класса по строке")
final class ClassByNameTest {

    @Test
    @DisplayName("класс находится по строковому имени")
    void loadsClassByName() throws ClassNotFoundException {
        MatcherAssert.assertThat(
            "class named by string cannot be loaded",
            new ClassByName("ru.sprbut.m01.model.Account").type(),
            Matchers.sameInstance(Account.class)
        );
    }

    @Test
    @DisplayName("несуществующее имя даёт ClassNotFoundException — связь через строку не проверяется компилятором")
    void dontLoadUnknownClass() {
        Assertions.assertThrows(
            ClassNotFoundException.class,
            () -> new ClassByName("ru.sprbut.NoSuchClass").type()
        );
    }
}
