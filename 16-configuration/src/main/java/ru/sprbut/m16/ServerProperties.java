/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m16;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Слайды 130–137: {@code @ConfigurationProperties} на record.
 *
 * <p>Компактный конструктор подставляет значения по умолчанию — это первый,
 * самый нижний уровень приоритета. Record здесь не случаен: биндер Spring Boot
 * умеет собирать неизменяемый объект через канонический конструктор, а
 * обычный биндинг JavaBeans (модуль 02) с неизменяемым объектом не работает.</p>
 *
 * @param host Узел
 * @param port Порт
 * @param sslEnabled Признак включённого TLS
 * @param timeout Таймаут: значения вида {@code 30s}, {@code 5m}, {@code PT1H}
 *  разбираются автоматически
 * @param allowedOrigins Разрешённые источники
 * @param headers Заголовки
 * @param retry Вложенная группа настроек — обычный record внутри
 * @since 1.0
 */
@ConfigurationProperties(prefix = "sprbut.server")
public record ServerProperties(
    String host,
    @Min(1) @Max(65_535) int port,
    boolean sslEnabled,
    Duration timeout,
    List<String> allowedOrigins,
    Map<String, String> headers,
    Retry retry) {

    /**
     * Значения по умолчанию задаются <b>в коде</b> — это первый, самый нижний
     * уровень приоритета (слайд 133).
     */
    public ServerProperties {
        host = Objects.requireNonNullElse(host, "localhost");
        if (port == 0) {
            port = 8080;
        }
        timeout = Objects.requireNonNullElse(timeout, Duration.ofSeconds(30));
        allowedOrigins = List.copyOf(Objects.requireNonNullElse(allowedOrigins, List.of()));
        headers = Map.copyOf(Objects.requireNonNullElse(headers, Map.of()));
        retry = Objects.requireNonNullElse(retry, new Retry(3, Duration.ofMillis(500)));
    }

    /**
     * Базовый адрес.
     * @return Базовый адрес
     */
    public String baseUrl() {
        final String scheme;
        if (this.sslEnabled) {
            scheme = "https";
        } else {
            scheme = "http";
        }
        return String.format("%s://%s:%s", scheme, this.host, this.port);
    }

    /**
     * Настройки повтора.
     *
     * @param attempts Число попыток
     * @param backoff Пауза между попытками
     * @since 1.0
     */
    public record Retry(int attempts, Duration backoff) {

        /**
         * Значения по умолчанию для группы повторов.
         */
        public Retry {
            if (attempts == 0) {
                attempts = 3;
            }
            backoff = Objects.requireNonNullElse(backoff, Duration.ofMillis(500));
        }
    }
}
