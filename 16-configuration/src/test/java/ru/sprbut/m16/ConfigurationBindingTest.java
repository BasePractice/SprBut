/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Слайды 129–137: конфигурация в коде и в файле.
 * @since 1.0
 */
@DisplayName("Слайды 129–137: конфигурация в коде и в файле")
final class ConfigurationBindingTest {
    @Nested
    @SpringBootTest
    @TestPropertySource(        properties = { "sprbut.server.host=overridden.example.com", "sprbut.server.port=9999" }
)
/**
 * Слайды 133–136: приоритеты источников.
 * @since 1.0
 */
    @DisplayName("Слайды 133–136: приоритеты источников")
    final class Priority {

        /**
         * Свойства.
         * @since 1.0
         */
        @Autowired
        ServerProperties properties;

        @Test
        @DisplayName("Более приоритетный источник перекрывает файл")
        void higherPrioritySourceWins() {
            MatcherAssert.assertThat(
                "cannot verify that higher priority source wins",
                this.properties.host(),
                Matchers.equalTo("overridden.example.com")
            );
            MatcherAssert.assertThat(
                "cannot verify that higher priority source wins",
                this.properties.port(),
                Matchers.equalTo(9999)
            );
        }

        @Test
        @DisplayName("Незатронутые ключи по-прежнему берутся из файла")
        void untouchedKeysComeFromTheFile() {
            MatcherAssert.assertThat(
                "cannot verify that untouched keys come from the file",
                this.properties.timeout(),
                Matchers.equalTo(Duration.ofSeconds(30))
            );
        }
    }

    @Nested
    @SpringBootTest
/**
 * @ConfigurationProperties: типизированная группа настроек.
 * @since 1.0
 */
    @DisplayName("@ConfigurationProperties: типизированная группа настроек")
    final class Binding {

        /**
         * Свойства.
         * @since 1.0
         */
        @Autowired
        ServerProperties properties;

        @Test
        @DisplayName("Значения приходят из application.yaml с приведением типов")
        void bindsFromYaml() {
            MatcherAssert.assertThat(
                "cannot verify that binds from yaml",
                this.properties.host(),
                Matchers.equalTo("api.example.com")
            );
            MatcherAssert.assertThat(
                "cannot verify that binds from yaml",
                this.properties.port(),
                Matchers.equalTo(8080)
            );
            MatcherAssert.assertThat(
                "cannot verify that binds from yaml",
                this.properties.sslEnabled(),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("Duration парсится из человекочитаемой записи")
        void bindsDuration() {
            MatcherAssert.assertThat(
                "cannot verify that binds duration",
                this.properties.timeout(),
                Matchers.equalTo(Duration.ofSeconds(30))
            );
            MatcherAssert.assertThat(
                "cannot verify that binds duration",
                this.properties.retry().backoff(),
                Matchers.equalTo(Duration.ofMillis(500))
            );
        }

        @Test
        @DisplayName("Списки и карты биндятся целиком")
        void bindsCollections() {
            MatcherAssert.assertThat(
                "cannot verify that binds collections",
                this.properties.allowedOrigins(),
                Matchers.contains("https://app.example.com", "https://admin.example.com")
            );
            MatcherAssert.assertThat(
                "map property cannot be bound entry by entry",
                this.properties.headers(),
                Matchers.hasEntry("X-Service", "sprbut")
            );
        }

        @Test
        @DisplayName("Вложенная группа настроек — тоже объект")
        void bindsNestedGroup() {
            MatcherAssert.assertThat(
                "cannot verify that binds nested group",
                this.properties.retry().attempts(),
                Matchers.equalTo(3)
            );
        }

        @Test
        @DisplayName("kebab-case в yaml соответствует camelCase в коде")
        void relaxedBindingWorks() {
            // ssl-enabled → sslEnabled, allowed-origins → allowedOrigins
            MatcherAssert.assertThat(
                "cannot verify that relaxed binding works",
                this.properties.sslEnabled(),
                Matchers.equalTo(false)
            );
            MatcherAssert.assertThat(
                "cannot verify that relaxed binding works",
                this.properties.allowedOrigins(),
                Matchers.not(Matchers.empty())
            );
        }

        @Test
        @DisplayName("Производные значения считаются в самом классе настроек")
        void derivedValuesLiveInTheClass() {
            MatcherAssert.assertThat(
                "cannot verify that derived values live in the class",
                this.properties.baseUrl(),
                Matchers.equalTo("http://api.example.com:8080")
            );
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("prod")
/**
 * Слайд 138: профили — application-{profile}.yaml.
 * @since 1.0
 */
    @DisplayName("Слайд 138: профили — application-{profile}.yaml")
    final class Profiles {

        /**
         * Свойства.
         * @since 1.0
         */
        @Autowired
        ServerProperties properties;

        @Test
        @DisplayName("Файл профиля перекрывает базовый там, где ключи заданы")
        void profileOverridesBaseFile() {
            MatcherAssert.assertThat(
                "cannot verify that profile overrides base file",
                this.properties.host(),
                Matchers.equalTo("api.prod.example.com")
            );
            MatcherAssert.assertThat(
                "cannot verify that profile overrides base file",
                this.properties.port(),
                Matchers.equalTo(443)
            );
            MatcherAssert.assertThat(
                "cannot verify that profile overrides base file",
                this.properties.sslEnabled(),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that profile overrides base file",
                this.properties.timeout(),
                Matchers.equalTo(Duration.ofSeconds(5))
            );
        }

        @Test
        @DisplayName("Незаданные в профиле ключи берутся из базового файла")
        void unspecifiedKeysFallBackToBaseFile() {
            MatcherAssert.assertThat(
                "cannot verify that unspecified keys fall back to base file",
                this.properties.allowedOrigins(),
                Matchers.contains("https://app.example.com", "https://admin.example.com")
            );
            MatcherAssert.assertThat(
                "cannot verify that unspecified keys fall back to base file",
                this.properties.retry().backoff(),
                Matchers.equalTo(Duration.ofMillis(500))
            );
        }

        @Test
        @DisplayName("Вложенные группы сливаются по отдельным ключам, а не заменяются целиком")
        void nestedGroupsAreMerged() {
            MatcherAssert.assertThat(
                "cannot verify that nested groups are merged",
                this.properties.retry().attempts(),
                Matchers.equalTo(5)
            );
            MatcherAssert.assertThat(
                "cannot verify that nested groups are merged",
                this.properties.retry().backoff(),
                Matchers.equalTo(Duration.ofMillis(500))
            );
        }

        @Test
        @DisplayName("Производные значения пересчитываются вместе с настройками")
        void derivedValuesFollow() {
            MatcherAssert.assertThat(
                "cannot verify that derived values follow",
                this.properties.baseUrl(),
                Matchers.equalTo("https://api.prod.example.com:443")
            );
        }
    }

    @Nested
    @SpringBootTest
/**
 * Слайд 137: @Value.
 * @since 1.0
 */
    @DisplayName("Слайд 137: @Value")
    final class ValueAnnotation {

        /**
         * Конфигурация.
         * @since 1.0
         */
        @Autowired
        ValueBasedConfig config;

        @Test
        @DisplayName("Существующий ключ читается и приводится к типу")
        void readsExistingKeys() {
            MatcherAssert.assertThat(
                "cannot verify that reads existing keys",
                this.config.host(),
                Matchers.equalTo("api.example.com")
            );
            MatcherAssert.assertThat(
                "cannot verify that reads existing keys",
                this.config.port(),
                Matchers.equalTo(8080)
            );
        }

        @Test
        @DisplayName("Отсутствующий ключ подставляет значение после двоеточия")
        void usesInlineDefault() {
            MatcherAssert.assertThat(
                "cannot verify that uses inline default",
                this.config.region(),
                Matchers.equalTo("eu-central")
            );
        }

        @Test
        @DisplayName("Список разбирается автоматически")
        void parsesList() {
            MatcherAssert.assertThat(
                "cannot verify that parses list",
                this.config.origins(),
                Matchers.hasSize(2)
            );
        }

        @Test
        @DisplayName("SpEL вычисляется при создании бина — @ConfigurationProperties так не умеет")
        void evaluatesSpel() {
            MatcherAssert.assertThat(
                "cannot verify that evaluates spel",
                this.config.doublePort(),
                Matchers.equalTo(16_160)
            );
        }
    }
}
