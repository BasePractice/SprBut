package ru.sprbut.m16;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Слайд 137: «{@code @Value} и {@code @ConfigurationProperties}».
 * <p>
 * {@code @ConfigurationProperties} — предпочтительный способ: типизированный,
 * группирующий связанные настройки, поддерживающий вложенность, коллекции,
 * {@link Duration} и валидацию. {@code @Value} остаётся для одиночных значений.
 * <p>
 * Это <b>constructor binding</b>: класс неизменяем, все поля {@code final},
 * сеттеров нет. Именно ради него Spring Boot пришлось учить отдельному режиму —
 * обычный биндинг JavaBeans (модуль 02) с неизменяемым объектом не работает.
 */
@ConfigurationProperties(prefix = "sprbut.server")
public record ServerProperties(

        String host,

        @Min(1) @Max(65_535)
        int port,

        boolean sslEnabled,

        /** Значения вида {@code 30s}, {@code 5m}, {@code PT1H} парсятся автоматически. */
        Duration timeout,

        List<String> allowedOrigins,

        Map<String, String> headers,

        /** Вложенная группа настроек — обычный record внутри. */
        Retry retry) {

    /**
     * Значения по умолчанию задаются <b>в коде</b> — это первый, самый нижний
     * уровень приоритета (слайд 133).
     */
    public ServerProperties {
        host = host == null ? "localhost" : host;
        port = port == 0 ? 8080 : port;
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        retry = retry == null ? new Retry(3, Duration.ofMillis(500)) : retry;
    }

    public String baseUrl() {
        return (sslEnabled ? "https://" : "http://") + host + ":" + port;
    }

    public record Retry(int attempts, Duration backoff) {

        public Retry {
            attempts = attempts == 0 ? 3 : attempts;
            backoff = backoff == null ? Duration.ofMillis(500) : backoff;
        }
    }
}
