package ru.sprbut.m16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 129–137: конфигурация в коде и в файле")
class ConfigurationBindingTest {

    @Nested
    @SpringBootTest
    @DisplayName("@ConfigurationProperties: типизированная группа настроек")
    class Binding {

        @Autowired
        ServerProperties properties;

        @Test
        @DisplayName("Значения приходят из application.yaml с приведением типов")
        void bindsFromYaml() {
            assertThat(properties.host()).isEqualTo("api.example.com");
            assertThat(properties.port()).isEqualTo(8080);
            assertThat(properties.sslEnabled()).isFalse();
        }

        @Test
        @DisplayName("Duration парсится из человекочитаемой записи")
        void bindsDuration() {
            assertThat(properties.timeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(properties.retry().backoff()).isEqualTo(Duration.ofMillis(500));
        }

        @Test
        @DisplayName("Списки и карты биндятся целиком")
        void bindsCollections() {
            assertThat(properties.allowedOrigins())
                    .containsExactly("https://app.example.com", "https://admin.example.com");
            assertThat(properties.headers())
                    .containsEntry("X-Service", "sprbut")
                    .containsEntry("X-Version", "1.0");
        }

        @Test
        @DisplayName("Вложенная группа настроек — тоже объект")
        void bindsNestedGroup() {
            assertThat(properties.retry().attempts()).isEqualTo(3);
        }

        @Test
        @DisplayName("kebab-case в yaml соответствует camelCase в коде")
        void relaxedBindingWorks() {
            // ssl-enabled → sslEnabled, allowed-origins → allowedOrigins
            assertThat(properties.sslEnabled()).isFalse();
            assertThat(properties.allowedOrigins()).isNotEmpty();
        }

        @Test
        @DisplayName("Производные значения считаются в самом классе настроек")
        void derivedValuesLiveInTheClass() {
            assertThat(properties.baseUrl()).isEqualTo("http://api.example.com:8080");
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("prod")
    @DisplayName("Слайд 138: профили — application-{profile}.yaml")
    class Profiles {

        @Autowired
        ServerProperties properties;

        @Test
        @DisplayName("Файл профиля перекрывает базовый там, где ключи заданы")
        void profileOverridesBaseFile() {
            assertThat(properties.host()).isEqualTo("api.prod.example.com");
            assertThat(properties.port()).isEqualTo(443);
            assertThat(properties.sslEnabled()).isTrue();
            assertThat(properties.timeout()).isEqualTo(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("Незаданные в профиле ключи берутся из базового файла")
        void unspecifiedKeysFallBackToBaseFile() {
            assertThat(properties.allowedOrigins())
                    .containsExactly("https://app.example.com", "https://admin.example.com");
            assertThat(properties.retry().backoff()).isEqualTo(Duration.ofMillis(500));
        }

        @Test
        @DisplayName("Вложенные группы сливаются по отдельным ключам, а не заменяются целиком")
        void nestedGroupsAreMerged() {
            assertThat(properties.retry().attempts()).isEqualTo(5);
            assertThat(properties.retry().backoff()).isEqualTo(Duration.ofMillis(500));
        }

        @Test
        @DisplayName("Производные значения пересчитываются вместе с настройками")
        void derivedValuesFollow() {
            assertThat(properties.baseUrl()).isEqualTo("https://api.prod.example.com:443");
        }
    }

    @Nested
    @SpringBootTest
    @DisplayName("Слайд 137: @Value")
    class ValueAnnotation {

        @Autowired
        ValueBasedConfig config;

        @Test
        @DisplayName("Существующий ключ читается и приводится к типу")
        void readsExistingKeys() {
            assertThat(config.host()).isEqualTo("api.example.com");
            assertThat(config.port()).isEqualTo(8080);
        }

        @Test
        @DisplayName("Отсутствующий ключ подставляет значение после двоеточия")
        void usesInlineDefault() {
            assertThat(config.region()).isEqualTo("eu-central");
        }

        @Test
        @DisplayName("Список разбирается автоматически")
        void parsesList() {
            assertThat(config.origins()).hasSize(2);
        }

        @Test
        @DisplayName("SpEL вычисляется при создании бина — @ConfigurationProperties так не умеет")
        void evaluatesSpel() {
            assertThat(config.doublePort()).isEqualTo(16_160);
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {
            "sprbut.server.host=overridden.example.com",
            "sprbut.server.port=9999"
    })
    @DisplayName("Слайды 133–136: приоритеты источников")
    class Priority {

        @Autowired
        ServerProperties properties;

        @Test
        @DisplayName("Более приоритетный источник перекрывает файл")
        void higherPrioritySourceWins() {
            assertThat(properties.host()).isEqualTo("overridden.example.com");
            assertThat(properties.port()).isEqualTo(9999);
        }

        @Test
        @DisplayName("Незатронутые ключи по-прежнему берутся из файла")
        void untouchedKeysComeFromTheFile() {
            assertThat(properties.timeout()).isEqualTo(Duration.ofSeconds(30));
        }
    }
}
