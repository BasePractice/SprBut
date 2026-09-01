/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m16;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Слайд 137: {@code @Value} — второй способ читать конфигурацию.
 *
 * <p>Он проще, но у него есть цена:
 * <ul>
 * <li>ключ — строка, опечатку компилятор не поймает;</li>
 * <li>значение по умолчанию пишется прямо в аннотации:
 * {@code ${key:default}} — и легко расходится с реальностью;</li>
 * <li>настройки размазаны по классам, вместо того чтобы лежать группой;</li>
 * <li>без значения по умолчанию отсутствие ключа роняет старт приложения.</li>
 * </ul>
 * Зато {@code @Value} умеет SpEL — то, чего не умеет
 * {@code @ConfigurationProperties}.</p>
 *
 * @since 1.0
 */
@Component
public class ValueBasedConfig {

    /**
     * Ключ есть в конфигурации.
     */
    private final String host;

    /**
     * Ключа нет — сработает значение по умолчанию после двоеточия.
     */
    private final String region;

    /**
     * Тип приводится автоматически.
     */
    private final int port;

    /**
     * Список разбирается по запятой — но <b>только из строки</b>.
     * YAML-список {@code allowed-origins: [a, b]} превращается в ключи
     * {@code allowed-origins[0]}, {@code allowed-origins[1]}, и {@code @Value}
     * их не видит: он умеет читать одиночный ключ, а не группу.
     * Для списков и карт есть {@code @ConfigurationProperties}.
     */
    private final List<String> origins;

    /**
     * SpEL: выражение вычисляется при создании бина.
     */
    private final int doublePort;

    /**
     * Основной конструктор.
     * @param @Value("${sprbut.server.host}" Значение
     */
    public ValueBasedConfig(
            @Value("${sprbut.server.host}") String host,
            @Value("${sprbut.server.region:eu-central}") String region,
            @Value("${sprbut.server.port}") int port,
            @Value("${sprbut.server.origins-csv}") List<String> origins,
            @Value("#{${sprbut.server.port} * 2}") int doublePort) {
        this.host = host;
        this.region = region;
        this.port = port;
        this.origins = origins;
        this.doublePort = doublePort;
    }

    /**
     * Узел.
     * @return Узел
     */
    public String host() {
        return this.host;
    }

    /**
     * Регион.
     * @return Регион
     */
    public String region() {
        return this.region;
    }

    /**
     * Порт.
     * @return Порт
     */
    public int port() {
        return this.port;
    }

    /**
     * Источники.
     * @return Источники
     */
    public List<String> origins() {
        return this.origins;
    }

    /**
     * Удвоенный порт.
     * @return Удвоенный порт
     */
    public int doublePort() {
        return this.doublePort;
    }
}
