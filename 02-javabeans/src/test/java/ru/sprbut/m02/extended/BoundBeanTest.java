/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m02.extended;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.classic.CustomerBean;
import ru.sprbut.m02.modern.CustomerRecord;

/**
 * Расширенный пример: биндинг конфигурации в JavaBean.
 * @since 1.0
 */
@DisplayName("Расширенный пример: биндинг конфигурации в JavaBean")
final class BoundBeanTest {

    @Test
    @DisplayName("строка превращается в число нужного типа")
    void convertsNumber() {
        MatcherAssert.assertThat(
            "string value cannot become an int property",
            new BoundBean<>(ServerProperties.class, Map.of("port", "8443")).result().bean().getPort(),
            Matchers.equalTo(8443)
        );
    }

    @Test
    @DisplayName("строка превращается в boolean")
    void convertsBoolean() {
        MatcherAssert.assertThat(
            "string value cannot become a boolean property",
            new BoundBean<>(ServerProperties.class, Map.of("sslEnabled", "true"))
                .result().bean().isSslEnabled(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("строка превращается в BigDecimal без потери точности")
    void convertsDecimal() {
        MatcherAssert.assertThat(
            "string value cannot become a decimal property",
            new BoundBean<>(ServerProperties.class, Map.of("rateLimit", "12.50"))
                .result().bean().getRateLimit(),
            Matchers.comparesEqualTo(new BigDecimal("12.50"))
        );
    }

    @Test
    @DisplayName("строка превращается в дату")
    void convertsDate() {
        MatcherAssert.assertThat(
            "string value cannot become a date property",
            new BoundBean<>(ServerProperties.class, Map.of("validUntil", "2030-01-31"))
                .result().bean().getValidUntil(),
            Matchers.equalTo(LocalDate.of(2030, 1, 31))
        );
    }

    @Test
    @DisplayName("enum читается без учёта регистра")
    void convertsEnumIgnoringCase() {
        MatcherAssert.assertThat(
            "enum value cannot be read case insensitively",
            new BoundBean<>(ServerProperties.class, Map.of("mode", "stage"))
                .result().bean().getMode(),
            Matchers.equalTo(ServerProperties.Mode.STAGE)
        );
    }

    @Test
    @DisplayName("незаданное свойство сохраняет значение по умолчанию из кода")
    void keepsCodeDefaults() {
        MatcherAssert.assertThat(
            "unset property cannot keep its code default",
            new BoundBean<>(ServerProperties.class, Map.of("port", "9090"))
                .result().bean().getHost(),
            Matchers.equalTo("localhost")
        );
    }

    @Test
    @DisplayName("kebab-case ключ приводится к имени свойства")
    void acceptsKebabCaseKeys() {
        MatcherAssert.assertThat(
            "kebab case key cannot reach its property",
            new BoundBean<>(ServerProperties.class, Map.of("ssl-enabled", "true"))
                .result().bean().isSslEnabled(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("snake_case ключ тоже приводится к имени свойства")
    void acceptsSnakeCaseKeys() {
        MatcherAssert.assertThat(
            "snake case key cannot reach its property",
            new BoundBean<>(ServerProperties.class, Map.of("timeout_millis", "100"))
                .result().bean().getTimeoutMillis(),
            Matchers.equalTo(100L)
        );
    }

    @Test
    @DisplayName("неизвестный ключ не роняет биндинг, а попадает в отчёт")
    void reportsIgnoredKeys() {
        MatcherAssert.assertThat(
            "unknown key cannot be reported instead of failing the binding",
            new BoundBean<>(ServerProperties.class, Map.of("host", "h", "unknown.key", "42"))
                .result().ignored(),
            Matchers.contains("unknown.key")
        );
    }

    @Test
    @DisplayName("отчёт перечисляет привязанные свойства")
    void reportsBoundProperties() {
        MatcherAssert.assertThat(
            "binding report cannot list the bound properties",
            new BoundBean<>(ServerProperties.class, Map.of("host", "h", "unknown.key", "42"))
                .result().bound(),
            Matchers.contains("host")
        );
    }

    @Test
    @DisplayName("непреобразуемое значение отбивается сразу, а не превращается в ноль")
    void dontSwallowMalformedValue() {
        MatcherAssert.assertThat(
            "malformed value cannot fail immediately with context",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new BoundBean<>(ServerProperties.class, Map.of("port", "восемь")).result()
            ).getMessage(),
            Matchers.containsString("port")
        );
    }

    @Test
    @DisplayName("record биндингу недоступен — соглашение на нём не работает")
    void dontBindRecord() {
        MatcherAssert.assertThat(
            "record cannot be rejected as a non JavaBean",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new BoundBean<>(CustomerRecord.class, Map.of("id", "C-1")).result()
            ).getMessage(),
            Matchers.containsString("не является JavaBean")
        );
    }

    @Test
    @DisplayName("одноимённые свойства копируются между бинами")
    void copiesMatchingProperties() {
        final CustomerBean source = new CustomerBean();
        source.setFirstName("Иван");
        final CustomerBean target = new CustomerBean();
        new CopiedProperties(source, target).list();
        MatcherAssert.assertThat(
            "matching property cannot be copied between beans",
            target.getFirstName(),
            Matchers.equalTo("Иван")
        );
    }
}
