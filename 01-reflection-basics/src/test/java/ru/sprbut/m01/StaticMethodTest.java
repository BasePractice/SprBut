/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m01;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

/**
 * Слайд 8: вызов статического метода без экземпляра.
 * @since 1.0
 */
@DisplayName("Слайд 8: вызов статического метода без экземпляра")
final class StaticMethodTest {

    @Test
    @DisplayName("статический метод вызывается с target = null")
    void invokesStaticMethod() {
        MatcherAssert.assertThat(
            "static method cannot be invoked without an instance",
            new StaticMethod(Account.class, "describeType").call(),
            Matchers.equalTo("Счёт типа CHECKING")
        );
    }

    @Test
    @DisplayName("перегрузки различаются по типам параметров, а не по имени")
    void resolvesOverloadByParameterTypes() {
        MatcherAssert.assertThat(
            "overload cannot be resolved by its parameter types",
            new StaticMethod(String.class, "valueOf", int.class).call(42),
            Matchers.equalTo("42")
        );
    }
}
