/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import java.io.File;
import java.util.ArrayList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 31: setAccessible и JPMS.
 * @since 1.0
 */
@DisplayName("Слайд 31: setAccessible и JPMS")
final class DeepAccessTest {

    @Test
    @DisplayName("свой класс открыт всегда — ограничения JPMS про границы модулей")
    void opensOwnClass() {
        MatcherAssert.assertThat(
            "own class cannot be open for deep reflection",
            new DeepAccess(Ours.class, "secret").attempt().succeeded(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("открытый флагом --add-opens пакет java.util доступен")
    void opensExplicitlyOpenedPackage() {
        MatcherAssert.assertThat(
            "package opened by the flag cannot be accessible",
            new DeepAccess(ArrayList.class, "elementData").attempt().succeeded(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("java.util открыт для нас, потому что флаг задан в surefire")
    void reportsOpenPackage() {
        MatcherAssert.assertThat(
            "opened package cannot report itself as open",
            new ModuleAccess(ArrayList.class).open(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("java.io экспортирован, но не открыт — это разные вещи")
    void separatesExportsFromOpens() {
        MatcherAssert.assertThat(
            "exported package cannot stay closed for deep reflection",
            new ModuleAccess(File.class).exported(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("не открытый пакет глубокую рефлексию не разрешает")
    void dontOpenClosedPackage() {
        MatcherAssert.assertThat(
            "closed package cannot refuse deep reflection",
            new ModuleAccess(File.class).open(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("класс из java.base принадлежит именованному модулю")
    void namesJdkModule() {
        MatcherAssert.assertThat(
            "JDK class cannot name its module",
            new ModuleAccess(ArrayList.class).moduleName(),
            Matchers.equalTo("java.base")
        );
    }

    @Test
    @DisplayName("код с classpath живёт в безымянном модуле")
    void reportsUnnamedModule() {
        MatcherAssert.assertThat(
            "classpath code cannot live in the unnamed module",
            new ModuleAccess(DeepAccessTest.class).moduleName(),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("несуществующее поле отличается от закрытого доступа")
    void separatesMissingFieldFromDeniedAccess() {
        MatcherAssert.assertThat(
            "missing field cannot be told apart from denied access",
            new DeepAccess(Ours.class, "nope").attempt().failure(),
            Matchers.equalTo("NoSuchFieldException")
        );
    }

    @SuppressWarnings("unused")
    private static final class Ours {

        /**
         * Секрет: поле существует только затем, чтобы до него достучались
         * рефлексией, из самого класса оно не читается.
         * @checkstyle ConstantUsageCheck (3 lines)
         */
        private final String secret = "доступно";
    }
}
