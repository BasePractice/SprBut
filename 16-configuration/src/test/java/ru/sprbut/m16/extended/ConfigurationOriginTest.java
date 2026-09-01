/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m16.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;

/**
 * Расширенный пример: СХЕМА 10 — откуда взялось значение.
 * @since 1.0
 */
@DisplayName("Расширенный пример: СХЕМА 10 — откуда взялось значение")
final class ConfigurationOriginTest {

    @Nested
    @SpringBootTest
/**
 * стек источников.
 * @since 1.0
 */
    @DisplayName("стек источников")
    class Stack {

        /**
         * Окружение.
         */
        @Autowired
        private ConfigurableEnvironment environment;

        @Test
        @DisplayName("Environment — это упорядоченный список источников, а не карта")
        void listsSourcesInOrder() {
            MatcherAssert.assertThat(
                "environment cannot expose its source stack",
                new ConfigurationOrigin(this.environment).stack(),
                Matchers.not(Matchers.hasSize(0))
            );
        }

        @Test
        @DisplayName("значение находится в первом же подходящем источнике")
        void resolvesFromFirstSource() {
            MatcherAssert.assertThat(
                "value cannot be resolved from the first matching source",
                new ConfigurationOrigin(this.environment)
                    .resolve("sprbut.server.host").orElseThrow().value(),
                Matchers.equalTo("api.example.com")
            );
        }

        @Test
        @DisplayName("несуществующий ключ не находится нигде")
        void findsNothingForUnknownKey() {
            MatcherAssert.assertThat(
                "unknown key cannot stay unresolved",
                new ConfigurationOrigin(this.environment).resolve("sprbut.no.such.key").isEmpty(),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("объяснение для ненайденного ключа так и говорит")
        void explainsMissingKey() {
            MatcherAssert.assertThat(
                "missing key cannot be explained plainly",
                new ConfigurationOrigin(this.environment).explain("sprbut.no.such.key"),
                Matchers.containsString("не найден ни в одном источнике")
            );
        }

        @Test
        @DisplayName("эффективная конфигурация по префиксу — то, что реально увидит приложение")
        void collectsEffectiveConfig() {
            MatcherAssert.assertThat(
                "effective config cannot collect the prefixed keys",
                new ConfigurationOrigin(this.environment).effective("sprbut.server"),
                Matchers.hasKey("sprbut.server.host")
            );
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "sprbut.server.host=inline.example.com")
/**
 * перекрытие значений.
 * @since 1.0
 */
    @DisplayName("перекрытие значений")
    class Overriding {

        /**
         * Окружение.
         */
        @Autowired
        private ConfigurableEnvironment environment;

        @Test
        @DisplayName("ключ встречается в двух источниках — выигрывает более приоритетный")
        void prefersHigherPriority() {
            MatcherAssert.assertThat(
                "higher priority source cannot win",
                new ConfigurationOrigin(this.environment)
                    .resolve("sprbut.server.host").orElseThrow().value(),
                Matchers.equalTo("inline.example.com")
            );
        }

        @Test
        @DisplayName("перекрытое значение из отчёта не пропадает")
        void keepsOverriddenValue() {
            MatcherAssert.assertThat(
                "overridden value cannot stay in the report",
                new ConfigurationOrigin(this.environment).occurrences("sprbut.server.host"),
                Matchers.hasSize(2)
            );
        }

        @Test
        @DisplayName("факт перекрытия виден явно")
        void reportsOverriding() {
            MatcherAssert.assertThat(
                "overriding cannot be reported explicitly",
                new ConfigurationOrigin(this.environment).overridden("sprbut.server.host"),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("неперекрытое значение перекрытым не считается")
        void dontReportPlainValueAsOverridden() {
            MatcherAssert.assertThat(
                "plain value cannot avoid the overriding verdict",
                new ConfigurationOrigin(this.environment).overridden("sprbut.server.timeout"),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("объяснение показывает и победителя, и перекрытое значение")
        void explainsBothValues() {
            MatcherAssert.assertThat(
                "explanation cannot show the overridden value",
                new ConfigurationOrigin(this.environment).explain("sprbut.server.host"),
                Matchers.containsString("перекрыто")
            );
        }
    }
}
