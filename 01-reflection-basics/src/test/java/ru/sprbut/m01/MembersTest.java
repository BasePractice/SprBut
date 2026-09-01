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
 * Слайд 6: getDeclaredXxx против getXxx.
 * @since 1.0
 */
@DisplayName("Слайд 6: getDeclaredXxx против getXxx")
final class MembersTest {

    @Test
    @DisplayName("getDeclaredFields() видит и приватные поля тоже")
    void listsDeclaredFields() {
        MatcherAssert.assertThat(
            "declared fields cannot include the private ones",
            new Members(Account.class).declaredFields(),
            Matchers.containsInAnyOrder("TYPE", "id", "owner", "balance", "blocked", "cachedLabel")
        );
    }

    @Test
    @DisplayName("getFields() показывает только публичные поля")
    void listsPublicFieldsOnly() {
        MatcherAssert.assertThat(
            "public fields cannot be limited to the public ones",
            new Members(Account.class).publicFields(),
            Matchers.contains("TYPE")
        );
    }

    @Test
    @DisplayName("приватные поля отбираются по флагу модификатора")
    void filtersPrivateFields() {
        MatcherAssert.assertThat(
            "private fields cannot be filtered by their modifier",
            new Members(Account.class).privateFields(),
            Matchers.containsInAnyOrder("id", "owner", "balance")
        );
    }

    @Test
    @DisplayName("статические поля отбираются по флагу модификатора")
    void filtersStaticFields() {
        MatcherAssert.assertThat(
            "static fields cannot be filtered by their modifier",
            new Members(Account.class).staticFields(),
            Matchers.contains("TYPE")
        );
    }

    @Test
    @DisplayName("getDeclaredMethods() перечисляет и приватные методы")
    void listsPrivateMethods() {
        MatcherAssert.assertThat(
            "declared methods cannot include the private ones",
            new Members(Account.class).declaredMethods(),
            Matchers.hasItems("applyFee", "block", "describeType")
        );
    }

    @Test
    @DisplayName("унаследованные методы Object в объявленные не попадают")
    void dontListInheritedMethods() {
        MatcherAssert.assertThat(
            "declared methods cannot leave the inherited ones out",
            new Members(Account.class).declaredMethods(),
            Matchers.not(Matchers.hasItems("equals", "hashCode"))
        );
    }
}
