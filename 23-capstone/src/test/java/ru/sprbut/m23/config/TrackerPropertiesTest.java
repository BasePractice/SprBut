package ru.sprbut.m23.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Конфигурация: приоритеты источников и профили")
final class TrackerPropertiesTest {

    @Test
    @DisplayName("значения по умолчанию живут в самом конструкторе")
    void fallsBackToCodeDefaults() {
        assertThat(
            "constructor cannot supply the default limit",
            new TrackerProperties(null, null, null).limit(),
            equalTo(100)
        );
    }

    @Test
    @DisplayName("свойства неизменяемы: сеттеров нет, значения приезжают один раз")
    void keepsValuesImmutable() {
        assertThat(
            "constructor binding cannot fix the title once",
            new TrackerProperties("Свой трекер", 5, false).title(),
            equalTo("Свой трекер")
        );
    }

    @Nested
    @SpringBootTest
    @DisplayName("значения из application.yaml")
    class FromFile {

        @Autowired
        private TrackerProperties settings;

        @Test
        @DisplayName("yaml перебивает значения по умолчанию из кода")
        void overridesCodeDefaults() {
            assertThat(
                "yaml cannot override the code default title",
                this.settings.title(),
                equalTo("SprBut Tracker")
            );
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("demo")
    @DisplayName("профиль demo")
    class FromProfile {

        @Autowired
        private TrackerProperties settings;

        @Test
        @DisplayName("профиль перебивает основной файл")
        void overridesBaseFile() {
            assertThat(
                "profile file cannot override the base yaml",
                this.settings.limit(),
                equalTo(3)
            );
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("demo")
    @TestPropertySource(properties = "tracker.limit=42")
    @DisplayName("явное свойство поверх профиля")
    class FromProperty {

        @Autowired
        private TrackerProperties settings;

        @Test
        @DisplayName("заданное снаружи свойство сильнее любого файла")
        void outranksEveryFile() {
            assertThat(
                "external property cannot outrank the profile file",
                this.settings.limit(),
                equalTo(42)
            );
        }
    }
}
