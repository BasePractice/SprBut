/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m26.reflection;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты загрузки расширения по имени класса.
 * @since 1.0
 */
@DisplayName("Слайд «AOT»: загрузка класса по имени работает на JVM и ломается в native")
final class PluginByNameTest {

    @Test
    @DisplayName("classloader находит класс, на который нет ни одной ссылки в коде")
    void loadsPluginByClassName() {
        MatcherAssert.assertThat(
            "classloader cannot instantiate a plugin named by string",
            new PluginByName("ru.sprbut.m26.reflection.CsvPlugin").plugin().name(),
            Matchers.equalTo("csv")
        );
    }

    @Test
    @DisplayName("незарегистрированное расширение на JVM работает точно так же")
    void loadsUnregisteredPluginOnJvm() {
        MatcherAssert.assertThat(
            "plugin missing from hints cannot work on a plain JVM",
            new PluginByName("ru.sprbut.m26.reflection.JsonPlugin").plugin().name(),
            Matchers.equalTo("json")
        );
    }

    @Test
    @DisplayName("отсутствующий класс превращается в понятную ошибку")
    void failsWithContextOnUnknownClass() {
        MatcherAssert.assertThat(
            "unknown plugin cannot report its own name in the failure",
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> new PluginByName("ru.sprbut.m26.reflection.XmlPlugin").plugin()
            ).getMessage(),
            Matchers.containsString("XmlPlugin")
        );
    }
}
