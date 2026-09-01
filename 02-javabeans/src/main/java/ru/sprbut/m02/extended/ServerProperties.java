/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// тема раздела — JavaBean как держатель конфигурации: имена свойств
// и одноимённые параметры сеттеров задаёт спецификация, по ним же
// биндер находит соответствие
// @checkstyle MemberNameCheck disable
// @checkstyle ParameterNameCheck disable
// @checkstyle HiddenFieldCheck disable
package ru.sprbut.m02.extended;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import ru.sprbut.m02.classic.BeanMap;

/**
 * Классический JavaBean в его самой типичной роли — держатель конфигурации.
 * Заполняется {@link BeanBinder} из карты, где ключу отвечает строка.
 *
 * <p>Это ровно та форма, которую в Spring Boot имеет класс с
 * {@code @ConfigurationProperties(prefix = "server")} (модуль 16).</p>
 *
 * @since 1.0
 */
// держатель конфигурации намеренно состоит из одних свойств:
// это ровно та форма, которую биндер умеет заполнять
@SuppressWarnings({
    "PMD.DataClass", "PMD.TooManyMethods", "PMD.ConstructorShouldDoInitialization"
})
public class ServerProperties implements Serializable {

    /**
     * Версия для сериализации.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Узел.
     */
    private String host = "localhost";

    /**
     * Порт.
     */
    private int port = 8080;

    /**
     * Признак включённого TLS.
     */
    private boolean sslEnabled;

    /**
     * Таймаут в миллисекундах.
     */
    private long timeoutMillis = 5_000L;

    /**
     * Режим.
     */
    private Mode mode = Mode.DEV;

    /**
     * Предел.
     */
    private BigDecimal rateLimit = BigDecimal.ZERO;

    /**
     * Срок действия.
     */
    private LocalDate validUntil;

    /**
     * Основной конструктор.
     */
    public ServerProperties() {
        // тело намеренно пустое
    }

    /**
     * Значение: узел.
     * @return Значение: узел
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Новое значение: узел.
     * @param host Узел
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Значение: порт.
     * @return Значение: порт
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Новое значение: порт.
     * @param port Порт
     */
    public void setPort(final int port) {
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException(
                String.format("Порт вне диапазона 1..65535: %d", port)
            );
        }
        this.port = port;
    }

    /**
     * Значение: признак включённого tls.
     * @return Значение: признак включённого tls
     */
    public boolean isSslEnabled() {
        return this.sslEnabled;
    }

    /**
     * Новое значение: признак включённого tls.
     * @param sslEnabled Признак включённого TLS
     */
    public void setSslEnabled(final boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    /**
     * Значение: таймаут в миллисекундах.
     * @return Значение: таймаут в миллисекундах
     */
    public long getTimeoutMillis() {
        return this.timeoutMillis;
    }

    /**
     * Новое значение: таймаут в миллисекундах.
     * @param timeoutMillis Таймаут в миллисекундах
     */
    public void setTimeoutMillis(final long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Значение: режим.
     * @return Значение: режим
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Новое значение: режим.
     * @param mode Режим
     */
    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    /**
     * Значение свойства {@code rateLimit}.
     * @return Значение свойства {@code rateLimit}
     */
    public BigDecimal getRateLimit() {
        return this.rateLimit;
    }

    /**
     * Новое значение свойства {@code rateLimit}.
     * @param rateLimit Предел
     */
    public void setRateLimit(final BigDecimal rateLimit) {
        this.rateLimit = rateLimit;
    }

    /**
     * Значение: срок действия.
     * @return Значение: срок действия
     */
    public LocalDate getValidUntil() {
        return this.validUntil;
    }

    /**
     * Новое значение: срок действия.
     * @param validUntil Срок действия
     */
    public void setValidUntil(final LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * Вычисляемое свойство: только чтение, биндер его не трогает.
     * @return Базовый адрес, собранный из остальных свойств
     */
    public String getBaseUrl() {
        final String scheme;
        if (this.sslEnabled) {
            scheme = "https";
        } else {
            scheme = "http";
        }
        return String.format("%s://%s:%d", scheme, this.host, this.port);
    }

    @Override
    public final String toString() {
        return String.format("ServerProperties%s", new BeanMap(this).text());
    }

    /**
     * Режим работы сервера.
     * @since 1.0
     */
    public enum Mode {

        /**
         * Разработка.
         */
        DEV,

        /**
         * Предпродакшн.
         */
        STAGE,

        /**
         * Промышленная эксплуатация.
         */
        PROD
    }
}
