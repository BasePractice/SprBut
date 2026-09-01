/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m23.config;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Конфигурация: приоритеты источников и профили.
 * @since 1.0
 */
@DisplayName("Конфигурация: приоритеты источников и профили")
final class TrackerPropertiesTest {

    @Test
    @DisplayName("значения по умолчанию живут в самом конструкторе")
    void fallsBackToCodeDefaults() {
        MatcherAssert.assertThat(
            "constructor cannot supply the default limit",
            new TrackerProperties(null, null, null).limit(),
            Matchers.equalTo(100)
        );
    }

    @Test
    @DisplayName("свойства неизменяемы: сеттеров нет, значения приезжают один раз")
    void keepsValuesImmutable() {
        MatcherAssert.assertThat(
            "constructor binding cannot fix the title once",
            new TrackerProperties("Свой трекер", 5, false).title(),
            Matchers.equalTo("Свой трекер")
        );
    }

    @Nested
    @SpringBootTest
/**
 * значения из application.yaml.
 * @since 1.0
 */
    @DisplayName("значения из application.yaml")
    class FromFile {

        /**
         * Настройки.
         */
        @Autowired
        private TrackerProperties settings;

        @Test
        @DisplayName("yaml перебивает значения по умолчанию из кода")
        void overridesCodeDefaults() {
            MatcherAssert.assertThat(
                "yaml cannot override the code default title",
                this.settings.title(),
                Matchers.equalTo("SprBut Tracker")
            );
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("demo")
/**
 * профиль demo.
 * @since 1.0
 */
    @DisplayName("профиль demo")
    class FromProfile {

        /**
         * Настройки.
         */
        @Autowired
        private TrackerProperties settings;

        @Test
        @DisplayName("профиль перебивает основной файл")
        void overridesBaseFile() {
            MatcherAssert.assertThat(
                "profile file cannot override the base yaml",
                this.settings.limit(),
                Matchers.equalTo(3)
            );
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("demo")
    @TestPropertySource(properties = "tracker.limit=42")
/**
 * явное свойство поверх профиля.
 * @since 1.0
 */
    @DisplayName("явное свойство поверх профиля")
    class FromProperty {

        /**
         * Настройки.
         */
        @Autowired
        private TrackerProperties settings;

        @Test
        @DisplayName("заданное снаружи свойство сильнее любого файла")
        void outranksEveryFile() {
            MatcherAssert.assertThat(
                "external property cannot outrank the profile file",
                this.settings.limit(),
                Matchers.equalTo(42)
            );
        }
    }
}
