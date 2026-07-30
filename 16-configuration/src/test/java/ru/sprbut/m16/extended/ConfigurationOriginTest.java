package ru.sprbut.m16.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@DisplayName("Расширенный пример: СХЕМА 10 — откуда взялось значение")
final class ConfigurationOriginTest {

    @Nested
    @SpringBootTest
    @DisplayName("стек источников")
    class Stack {

        @Autowired
        private ConfigurableEnvironment environment;

        @Test
        @DisplayName("Environment — это упорядоченный список источников, а не карта")
        void listsSourcesInOrder() {
            assertThat(
                "environment cannot expose its source stack",
                new ConfigurationOrigin(this.environment).stack(),
                not(hasSize(0))
            );
        }

        @Test
        @DisplayName("значение находится в первом же подходящем источнике")
        void resolvesFromFirstSource() {
            assertThat(
                "value cannot be resolved from the first matching source",
                new ConfigurationOrigin(this.environment)
                    .resolve("sprbut.server.host").orElseThrow().value(),
                equalTo("api.example.com")
            );
        }

        @Test
        @DisplayName("несуществующий ключ не находится нигде")
        void findsNothingForUnknownKey() {
            assertThat(
                "unknown key cannot stay unresolved",
                new ConfigurationOrigin(this.environment).resolve("sprbut.no.such.key").isEmpty(),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("объяснение для ненайденного ключа так и говорит")
        void explainsMissingKey() {
            assertThat(
                "missing key cannot be explained plainly",
                new ConfigurationOrigin(this.environment).explain("sprbut.no.such.key"),
                containsString("не найден ни в одном источнике")
            );
        }

        @Test
        @DisplayName("эффективная конфигурация по префиксу — то, что реально увидит приложение")
        void collectsEffectiveConfig() {
            assertThat(
                "effective config cannot collect the prefixed keys",
                new ConfigurationOrigin(this.environment).effective("sprbut.server"),
                hasKey("sprbut.server.host")
            );
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "sprbut.server.host=inline.example.com")
    @DisplayName("перекрытие значений")
    class Overriding {

        @Autowired
        private ConfigurableEnvironment environment;

        @Test
        @DisplayName("ключ встречается в двух источниках — выигрывает более приоритетный")
        void prefersHigherPriority() {
            assertThat(
                "higher priority source cannot win",
                new ConfigurationOrigin(this.environment)
                    .resolve("sprbut.server.host").orElseThrow().value(),
                equalTo("inline.example.com")
            );
        }

        @Test
        @DisplayName("перекрытое значение из отчёта не пропадает")
        void keepsOverriddenValue() {
            assertThat(
                "overridden value cannot stay in the report",
                new ConfigurationOrigin(this.environment).occurrences("sprbut.server.host"),
                hasSize(2)
            );
        }

        @Test
        @DisplayName("факт перекрытия виден явно")
        void reportsOverriding() {
            assertThat(
                "overriding cannot be reported explicitly",
                new ConfigurationOrigin(this.environment).overridden("sprbut.server.host"),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("неперекрытое значение перекрытым не считается")
        void dontReportPlainValueAsOverridden() {
            assertThat(
                "plain value cannot avoid the overriding verdict",
                new ConfigurationOrigin(this.environment).overridden("sprbut.server.timeout"),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("объяснение показывает и победителя, и перекрытое значение")
        void explainsBothValues() {
            assertThat(
                "explanation cannot show the overridden value",
                new ConfigurationOrigin(this.environment).explain("sprbut.server.host"),
                containsString("перекрыто")
            );
        }
    }
}
