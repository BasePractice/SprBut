package ru.sprbut.m02.classic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Правило java.beans: имя свойства из имени метода")
final class PropertyKeyTest {

    @Test
    @DisplayName("обычное имя начинается со строчной буквы")
    void decapitalizesOrdinaryName() {
        assertThat(
            "ordinary name cannot be decapitalised",
            new PropertyKey("Name").decapitalized(),
            equalTo("name")
        );
    }

    @Test
    @DisplayName("аббревиатуру из двух заглавных подряд трогать нельзя")
    void dontDecapitalizeAbbreviation() {
        assertThat(
            "abbreviation cannot survive decapitalisation untouched",
            new PropertyKey("URL").decapitalized(),
            equalTo("URL")
        );
    }

    @Test
    @DisplayName("имя метода строится из имени свойства заглавной буквой")
    void capitalizesProperty() {
        assertThat(
            "property name cannot be capitalised for the method name",
            new PropertyKey("firstName").capitalized(),
            equalTo("FirstName")
        );
    }

    @Test
    @DisplayName("kebab-case из конфигурации становится camelCase")
    void convertsKebabCase() {
        assertThat(
            "kebab case key cannot become camel case",
            new PropertyKey("first-name").camelCase(),
            equalTo("firstName")
        );
    }

    @Test
    @DisplayName("snake_case тоже становится camelCase")
    void convertsSnakeCase() {
        assertThat(
            "snake case key cannot become camel case",
            new PropertyKey("first_name").camelCase(),
            equalTo("firstName")
        );
    }

    @Test
    @DisplayName("уже готовое имя не меняется")
    void keepsCamelCaseAsIs() {
        assertThat(
            "camel case key cannot survive unchanged",
            new PropertyKey("firstName").camelCase(),
            equalTo("firstName")
        );
    }
}
