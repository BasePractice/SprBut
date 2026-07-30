package ru.sprbut.m02.extended;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.classic.CustomerBean;
import ru.sprbut.m02.modern.CustomerRecord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Расширенный пример: биндинг конфигурации в JavaBean")
final class BoundBeanTest {

    @Test
    @DisplayName("строка превращается в число нужного типа")
    void convertsNumber() {
        assertThat(
            "string value cannot become an int property",
            new BoundBean<>(ServerProperties.class, Map.of("port", "8443")).result().bean().getPort(),
            equalTo(8443)
        );
    }

    @Test
    @DisplayName("строка превращается в boolean")
    void convertsBoolean() {
        assertThat(
            "string value cannot become a boolean property",
            new BoundBean<>(ServerProperties.class, Map.of("sslEnabled", "true"))
                .result().bean().isSslEnabled(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("строка превращается в BigDecimal без потери точности")
    void convertsDecimal() {
        assertThat(
            "string value cannot become a decimal property",
            new BoundBean<>(ServerProperties.class, Map.of("rateLimit", "12.50"))
                .result().bean().getRateLimit(),
            comparesEqualTo(new BigDecimal("12.50"))
        );
    }

    @Test
    @DisplayName("строка превращается в дату")
    void convertsDate() {
        assertThat(
            "string value cannot become a date property",
            new BoundBean<>(ServerProperties.class, Map.of("validUntil", "2030-01-31"))
                .result().bean().getValidUntil(),
            equalTo(LocalDate.of(2030, 1, 31))
        );
    }

    @Test
    @DisplayName("enum читается без учёта регистра")
    void convertsEnumIgnoringCase() {
        assertThat(
            "enum value cannot be read case insensitively",
            new BoundBean<>(ServerProperties.class, Map.of("mode", "stage"))
                .result().bean().getMode(),
            equalTo(ServerProperties.Mode.STAGE)
        );
    }

    @Test
    @DisplayName("незаданное свойство сохраняет значение по умолчанию из кода")
    void keepsCodeDefaults() {
        assertThat(
            "unset property cannot keep its code default",
            new BoundBean<>(ServerProperties.class, Map.of("port", "9090"))
                .result().bean().getHost(),
            equalTo("localhost")
        );
    }

    @Test
    @DisplayName("kebab-case ключ приводится к имени свойства")
    void acceptsKebabCaseKeys() {
        assertThat(
            "kebab case key cannot reach its property",
            new BoundBean<>(ServerProperties.class, Map.of("ssl-enabled", "true"))
                .result().bean().isSslEnabled(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("snake_case ключ тоже приводится к имени свойства")
    void acceptsSnakeCaseKeys() {
        assertThat(
            "snake case key cannot reach its property",
            new BoundBean<>(ServerProperties.class, Map.of("timeout_millis", "100"))
                .result().bean().getTimeoutMillis(),
            equalTo(100L)
        );
    }

    @Test
    @DisplayName("неизвестный ключ не роняет биндинг, а попадает в отчёт")
    void reportsIgnoredKeys() {
        assertThat(
            "unknown key cannot be reported instead of failing the binding",
            new BoundBean<>(ServerProperties.class, Map.of("host", "h", "unknown.key", "42"))
                .result().ignored(),
            contains("unknown.key")
        );
    }

    @Test
    @DisplayName("отчёт перечисляет привязанные свойства")
    void reportsBoundProperties() {
        assertThat(
            "binding report cannot list the bound properties",
            new BoundBean<>(ServerProperties.class, Map.of("host", "h", "unknown.key", "42"))
                .result().bound(),
            contains("host")
        );
    }

    @Test
    @DisplayName("непреобразуемое значение отбивается сразу, а не превращается в ноль")
    void dontSwallowMalformedValue() {
        assertThat(
            "malformed value cannot fail immediately with context",
            assertThrows(
                IllegalArgumentException.class,
                () -> new BoundBean<>(ServerProperties.class, Map.of("port", "восемь")).result()
            ).getMessage(),
            containsString("port")
        );
    }

    @Test
    @DisplayName("record биндингу недоступен — соглашение на нём не работает")
    void dontBindRecord() {
        assertThat(
            "record cannot be rejected as a non JavaBean",
            assertThrows(
                IllegalArgumentException.class,
                () -> new BoundBean<>(CustomerRecord.class, Map.of("id", "C-1")).result()
            ).getMessage(),
            containsString("не является JavaBean")
        );
    }

    @Test
    @DisplayName("одноимённые свойства копируются между бинами")
    void copiesMatchingProperties() {
        CustomerBean source = new CustomerBean();
        source.setFirstName("Иван");
        CustomerBean target = new CustomerBean();
        new CopiedProperties(source, target).list();
        assertThat(
            "matching property cannot be copied between beans",
            target.getFirstName(),
            equalTo("Иван")
        );
    }
}
