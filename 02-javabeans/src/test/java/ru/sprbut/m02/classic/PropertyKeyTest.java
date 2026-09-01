/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m02.classic;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Правило java.beans: имя свойства из имени метода.
 * @since 1.0
 */
@DisplayName("Правило java.beans: имя свойства из имени метода")
final class PropertyKeyTest {

    @Test
    @DisplayName("обычное имя начинается со строчной буквы")
    void decapitalizesOrdinaryName() {
        MatcherAssert.assertThat(
            "ordinary name cannot be decapitalised",
            new PropertyKey("Name").decapitalized(),
            Matchers.equalTo("name")
        );
    }

    @Test
    @DisplayName("аббревиатуру из двух заглавных подряд трогать нельзя")
    void dontDecapitalizeAbbreviation() {
        MatcherAssert.assertThat(
            "abbreviation cannot survive decapitalisation untouched",
            new PropertyKey("URL").decapitalized(),
            Matchers.equalTo("URL")
        );
    }

    @Test
    @DisplayName("имя метода строится из имени свойства заглавной буквой")
    void capitalizesProperty() {
        MatcherAssert.assertThat(
            "property name cannot be capitalised for the method name",
            new PropertyKey("firstName").capitalized(),
            Matchers.equalTo("FirstName")
        );
    }

    @Test
    @DisplayName("kebab-case из конфигурации становится camelCase")
    void convertsKebabCase() {
        MatcherAssert.assertThat(
            "kebab case key cannot become camel case",
            new PropertyKey("first-name").camelCase(),
            Matchers.equalTo("firstName")
        );
    }

    @Test
    @DisplayName("snake_case тоже становится camelCase")
    void convertsSnakeCase() {
        MatcherAssert.assertThat(
            "snake case key cannot become camel case",
            new PropertyKey("first_name").camelCase(),
            Matchers.equalTo("firstName")
        );
    }

    @Test
    @DisplayName("уже готовое имя не меняется")
    void keepsCamelCaseAsIs() {
        MatcherAssert.assertThat(
            "camel case key cannot survive unchanged",
            new PropertyKey("firstName").camelCase(),
            Matchers.equalTo("firstName")
        );
    }
}
