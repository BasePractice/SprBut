package ru.sprbut.m16.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: СХЕМА 10 — откуда взялось значение")
class ConfigurationOriginTest {

    @Nested
    @SpringBootTest
    @DisplayName("Стек источников")
    class Stack {

        @Autowired
        ConfigurableEnvironment environment;

        @Test
        @DisplayName("Environment — это упорядоченный список источников")
        void environmentIsAnOrderedStack() {
            assertThat(ConfigurationOrigin.priorityStack(environment))
                    .isNotEmpty()
                    .anyMatch(name -> name.contains("systemProperties"))
                    .anyMatch(name -> name.contains("systemEnvironment"))
                    .anyMatch(name -> name.contains("application.yaml"));
        }

        @Test
        @DisplayName("Системные свойства стоят выше файла конфигурации")
        void systemPropertiesOutrankTheFile() {
            var stack = ConfigurationOrigin.priorityStack(environment);
            int systemProperties = indexOfContaining(stack, "systemProperties");
            int yaml = indexOfContaining(stack, "application.yaml");

            assertThat(systemProperties)
                    .as("чем меньше индекс, тем выше приоритет")
                    .isLessThan(yaml);
        }

        @Test
        @DisplayName("Значение находится в первом же подходящем источнике")
        void valueComesFromTheFirstMatchingSource() {
            assertThat(ConfigurationOrigin.resolve(environment, "sprbut.server.host"))
                    .get()
                    .satisfies(origin -> {
                        assertThat(origin.value()).isEqualTo("api.example.com");
                        assertThat(origin.propertySource()).contains("application.yaml");
                    });
        }

        @Test
        @DisplayName("Несуществующий ключ не находится нигде")
        void missingKeyIsReportedClearly() {
            assertThat(ConfigurationOrigin.resolve(environment, "sprbut.no.such.key")).isEmpty();
            assertThat(ConfigurationOrigin.explain(environment, "sprbut.no.such.key"))
                    .contains("не найден ни в одном источнике");
        }

        @Test
        @DisplayName("Эффективная конфигурация по префиксу — то, что реально увидит приложение")
        void showsEffectiveConfig() {
            assertThat(ConfigurationOrigin.effectiveConfig(environment, "sprbut.server"))
                    .containsKeys("sprbut.server.host", "sprbut.server.port",
                            "sprbut.server.timeout");
        }

        private int indexOfContaining(java.util.List<String> stack, String fragment) {
            for (int i = 0; i < stack.size(); i++) {
                if (stack.get(i).contains(fragment)) {
                    return i;
                }
            }
            throw new AssertionError("Нет источника, содержащего '" + fragment + "' в " + stack);
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = "sprbut.server.host=inline.example.com")
    @DisplayName("Перекрытие значений")
    class Overriding {

        @Autowired
        ConfigurableEnvironment environment;

        @Test
        @DisplayName("Ключ встречается в двух источниках — выигрывает более приоритетный")
        void higherPriorityWins() {
            var occurrences = ConfigurationOrigin.allOccurrences(environment, "sprbut.server.host");

            assertThat(occurrences).hasSizeGreaterThanOrEqualTo(2);
            assertThat(occurrences.get(0).value()).isEqualTo("inline.example.com");
            assertThat(occurrences.get(1).value()).isEqualTo("api.example.com");
            assertThat(occurrences.get(0).priority()).isLessThan(occurrences.get(1).priority());
        }

        @Test
        @DisplayName("Факт перекрытия виден явно")
        void overrideIsDetected() {
            assertThat(ConfigurationOrigin.isOverridden(environment, "sprbut.server.host")).isTrue();
            assertThat(ConfigurationOrigin.isOverridden(environment, "sprbut.server.timeout"))
                    .isFalse();
        }

        @Test
        @DisplayName("Объяснение читается человеком: значение, источник и что перекрыто")
        void explanationIsHumanReadable() {
            assertThat(ConfigurationOrigin.explain(environment, "sprbut.server.host"))
                    .contains("inline.example.com")
                    .contains("перекрыто: api.example.com");
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("prod")
    @DisplayName("Профиль как отдельный источник")
    class ProfileSource {

        @Autowired
        ConfigurableEnvironment environment;

        @Test
        @DisplayName("Файл профиля добавляется в стек выше базового")
        void profileFileOutranksTheBaseFile() {
            var occurrences = ConfigurationOrigin.allOccurrences(environment, "sprbut.server.host");

            assertThat(occurrences).hasSize(2);
            assertThat(occurrences.get(0).propertySource()).contains("application-prod.yaml");
            assertThat(occurrences.get(0).value()).isEqualTo("api.prod.example.com");
            assertThat(occurrences.get(1).propertySource()).contains("application.yaml");
        }

        @Test
        @DisplayName("Ключи, которых нет в профиле, приходят из базового файла — без перекрытия")
        void baseFileFillsTheGaps() {
            var occurrences = ConfigurationOrigin
                    .allOccurrences(environment, "sprbut.server.allowed-origins[0]");

            assertThat(occurrences).hasSize(1);
            assertThat(occurrences.get(0).propertySource()).contains("application.yaml");
        }
    }
}
