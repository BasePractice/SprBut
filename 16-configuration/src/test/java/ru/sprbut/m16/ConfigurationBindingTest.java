package ru.sprbut.m16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            assertThat(
                "cannot verify that binds from yaml",
                properties.host(),
                equalTo("api.example.com")
            );
            assertThat(
                "cannot verify that binds from yaml",
                properties.port(),
                equalTo(8080)
            );
            assertThat(
                "cannot verify that binds from yaml",
                properties.sslEnabled(),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("Duration парсится из человекочитаемой записи")
        void bindsDuration() {
            assertThat(
                "cannot verify that binds duration",
                properties.timeout(),
                equalTo(Duration.ofSeconds(30))
            );
            assertThat(
                "cannot verify that binds duration",
                properties.retry().backoff(),
                equalTo(Duration.ofMillis(500))
            );
        }

        @Test
        @DisplayName("Списки и карты биндятся целиком")
        void bindsCollections() {
            assertThat(
                "cannot verify that binds collections",
                properties.allowedOrigins(),
                contains("https://app.example.com", "https://admin.example.com")
            );
            assertThat(
                "map property cannot be bound entry by entry",
                properties.headers(),
                hasEntry("X-Service", "sprbut")
            );
        }

        @Test
        @DisplayName("Вложенная группа настроек — тоже объект")
        void bindsNestedGroup() {
            assertThat(
                "cannot verify that binds nested group",
                properties.retry().attempts(),
                equalTo(3)
            );
        }

        @Test
        @DisplayName("kebab-case в yaml соответствует camelCase в коде")
        void relaxedBindingWorks() {
            // ssl-enabled → sslEnabled, allowed-origins → allowedOrigins
            assertThat(
                "cannot verify that relaxed binding works",
                properties.sslEnabled(),
                equalTo(false)
            );
            assertThat(
                "cannot verify that relaxed binding works",
                properties.allowedOrigins(),
                not(empty())
            );
        }

        @Test
        @DisplayName("Производные значения считаются в самом классе настроек")
        void derivedValuesLiveInTheClass() {
            assertThat(
                "cannot verify that derived values live in the class",
                properties.baseUrl(),
                equalTo("http://api.example.com:8080")
            );
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
            assertThat(
                "cannot verify that profile overrides base file",
                properties.host(),
                equalTo("api.prod.example.com")
            );
            assertThat(
                "cannot verify that profile overrides base file",
                properties.port(),
                equalTo(443)
            );
            assertThat(
                "cannot verify that profile overrides base file",
                properties.sslEnabled(),
                equalTo(true)
            );
            assertThat(
                "cannot verify that profile overrides base file",
                properties.timeout(),
                equalTo(Duration.ofSeconds(5))
            );
        }

        @Test
        @DisplayName("Незаданные в профиле ключи берутся из базового файла")
        void unspecifiedKeysFallBackToBaseFile() {
            assertThat(
                "cannot verify that unspecified keys fall back to base file",
                properties.allowedOrigins(),
                contains("https://app.example.com", "https://admin.example.com")
            );
            assertThat(
                "cannot verify that unspecified keys fall back to base file",
                properties.retry().backoff(),
                equalTo(Duration.ofMillis(500))
            );
        }

        @Test
        @DisplayName("Вложенные группы сливаются по отдельным ключам, а не заменяются целиком")
        void nestedGroupsAreMerged() {
            assertThat(
                "cannot verify that nested groups are merged",
                properties.retry().attempts(),
                equalTo(5)
            );
            assertThat(
                "cannot verify that nested groups are merged",
                properties.retry().backoff(),
                equalTo(Duration.ofMillis(500))
            );
        }

        @Test
        @DisplayName("Производные значения пересчитываются вместе с настройками")
        void derivedValuesFollow() {
            assertThat(
                "cannot verify that derived values follow",
                properties.baseUrl(),
                equalTo("https://api.prod.example.com:443")
            );
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
            assertThat(
                "cannot verify that reads existing keys",
                config.host(),
                equalTo("api.example.com")
            );
            assertThat(
                "cannot verify that reads existing keys",
                config.port(),
                equalTo(8080)
            );
        }

        @Test
        @DisplayName("Отсутствующий ключ подставляет значение после двоеточия")
        void usesInlineDefault() {
            assertThat(
                "cannot verify that uses inline default",
                config.region(),
                equalTo("eu-central")
            );
        }

        @Test
        @DisplayName("Список разбирается автоматически")
        void parsesList() {
            assertThat(
                "cannot verify that parses list",
                config.origins(),
                hasSize(2)
            );
        }

        @Test
        @DisplayName("SpEL вычисляется при создании бина — @ConfigurationProperties так не умеет")
        void evaluatesSpel() {
            assertThat(
                "cannot verify that evaluates spel",
                config.doublePort(),
                equalTo(16_160)
            );
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
            assertThat(
                "cannot verify that higher priority source wins",
                properties.host(),
                equalTo("overridden.example.com")
            );
            assertThat(
                "cannot verify that higher priority source wins",
                properties.port(),
                equalTo(9999)
            );
        }

        @Test
        @DisplayName("Незатронутые ключи по-прежнему берутся из файла")
        void untouchedKeysComeFromTheFile() {
            assertThat(
                "cannot verify that untouched keys come from the file",
                properties.timeout(),
                equalTo(Duration.ofSeconds(30))
            );
        }
    }
}
