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
 * Слайд 6: расшифровка битовой маски модификаторов.
 * @since 1.0
 */
@DisplayName("Слайд 6: расшифровка битовой маски модификаторов")
final class ModifiersTest {

    @Test
    @DisplayName("private final читается словами")
    void describesPrivateFinal() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "modifier mask cannot be read as private final",
            new Modifiers(Account.class.getDeclaredField("id")).text(),
            Matchers.equalTo("private final")
        );
    }

    @Test
    @DisplayName("public static final читается словами")
    void describesPublicStaticFinal() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "modifier mask cannot be read as public static final",
            new Modifiers(Account.class.getDeclaredField("TYPE")).text(),
            Matchers.equalTo("public static final")
        );
    }

    @Test
    @DisplayName("поле без модификаторов доступа даёт пустую строку")
    void describesPackagePrivate() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "package private field cannot yield an empty description",
            new Modifiers(Account.class.getDeclaredField("cachedLabel")).text(),
            Matchers.equalTo("transient")
        );
    }

    @Test
    @DisplayName("final распознаётся отдельным флагом")
    void detectsFinal() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "final flag cannot be detected",
            new Modifiers(Account.class.getDeclaredField("id")).isFinal(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("изменяемое поле финальным не считается")
    void dontMarkMutableFieldFinal() {
        MatcherAssert.assertThat(
            "mutable field cannot avoid the final flag",
            new Modifiers(new Declared(Account.class).field("owner")).isFinal(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("static распознаётся отдельным флагом")
    void detectsStatic() {
        MatcherAssert.assertThat(
            "static flag cannot be detected",
            new Modifiers(new Declared(Account.class).field("TYPE")).isStatic(),
            Matchers.equalTo(true)
        );
    }
}
