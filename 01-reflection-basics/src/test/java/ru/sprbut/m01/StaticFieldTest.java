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
 * Слайд 7: статическое поле читается без экземпляра.
 * @since 1.0
 */
@DisplayName("Слайд 7: статическое поле читается без экземпляра")
final class StaticFieldTest {

    @Test
    @DisplayName("значение статического поля берётся через get(null)")
    void readsStaticField() {
        MatcherAssert.assertThat(
            "static field cannot be read without an instance",
            new StaticField(Account.class, "TYPE").value(),
            Matchers.equalTo("CHECKING")
        );
    }
}
