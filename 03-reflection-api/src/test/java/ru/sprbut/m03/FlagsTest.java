/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m03;

import java.lang.reflect.Modifier;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

/**
 * СХЕМА 1: узел Modifier.
 * @since 1.0
 */
@DisplayName("СХЕМА 1: узел Modifier")
final class FlagsTest {

    @Test
    @DisplayName("маска раскладывается на отдельные флаги")
    void listsFlags() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "modifier mask cannot be split into flags",
            new Flags(Order.class.getDeclaredField("id")).names(),
            Matchers.contains("private", "final")
        );
    }

    @Test
    @DisplayName("volatile — такой же флаг, как остальные")
    void listsVolatile() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "volatile flag cannot appear among the others",
            new Flags(Order.class.getDeclaredField("paid")).names(),
            Matchers.hasItems("private", "volatile")
        );
    }

    @Test
    @DisplayName("javap печатает модификаторы в каноническом порядке")
    void printsCanonicalOrder() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "canonical modifier order cannot be printed",
            new Flags(Order.class.getDeclaredField("STATUS_NEW")).text(),
            Matchers.equalTo("public static final")
        );
    }

    @Test
    @DisplayName("package-private собственного бита не имеет — это отсутствие трёх других")
    void detectsPackagePrivate() {
        MatcherAssert.assertThat(
            "package private cannot be detected by the absence of the other flags",
            new Flags(0).packagePrivate(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("public package-private не является")
    void dontCallPublicPackagePrivate() {
        MatcherAssert.assertThat(
            "public member cannot avoid the package private verdict",
            new Flags(Modifier.PUBLIC).packagePrivate(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("volatile для класса недопустим — у каждого элемента своя маска допустимых флагов")
    void rejectsVolatileOnClass() {
        MatcherAssert.assertThat(
            "volatile cannot be rejected for a class",
            new Flags(Modifier.VOLATILE).validForClass(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("для поля volatile допустим")
    void acceptsVolatileOnField() {
        MatcherAssert.assertThat(
            "volatile cannot be accepted for a field",
            new Flags(Modifier.VOLATILE).validForField(),
            Matchers.equalTo(true)
        );
    }
}
