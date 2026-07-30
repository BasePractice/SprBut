package ru.sprbut.m02.extended;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Классический JavaBean в его самой типичной роли — держатель конфигурации.
 * Заполняется {@link BeanBinder} из карты «ключ → строка».
 * <p>
 * Это ровно та форма, которую в Spring Boot имеет класс с
 * {@code @ConfigurationProperties(prefix = "server")} (модуль 16).
 */
public class ServerProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Mode { DEV, STAGE, PROD }

    private String host = "localhost";
    private int port = 8080;
    private boolean sslEnabled;
    private long timeoutMillis = 5_000L;
    private Mode mode = Mode.DEV;
    private BigDecimal rateLimit = BigDecimal.ZERO;
    private LocalDate validUntil;

    public ServerProperties() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException("Порт вне диапазона 1..65535: " + port);
        }
        this.port = port;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public BigDecimal getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(BigDecimal rateLimit) {
        this.rateLimit = rateLimit;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    /** Вычисляемое свойство: только чтение, биндер его не трогает. */
    public String getBaseUrl() {
        return (sslEnabled ? "https://" : "http://") + host + ":" + port;
    }

    @Override
    public String toString() {
        return "ServerProperties" + new ru.sprbut.m02.classic.BeanMap(this).text();
    }
}
