/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m19.greeter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки стартера. Префикс — имя стартера, это соглашение всей экосистемы:
 * {@code spring.datasource.*}, {@code spring.jpa.*}, {@code sprbut.greeter.*}.
 *
 * <p>Модуль подключает {@code spring-boot-configuration-processor} — он собирает
 * из этого класса файл {@code META-INF/spring-configuration-metadata.json},
 * благодаря которому IDE подсказывает ключи в {@code application.yaml}.
 * Это ещё один annotation processor (модуль 07) в реальном деле.</p>
 *
 * @since 1.0
 */
@ConfigurationProperties(prefix = "sprbut.greeter")
public class GreeterProperties {
    /**
     * Шаблон приветствия; {@code {name}} подставляется.
     */
    private String template = "Привет, {name}!";

    /**
     * Писать ли приветствие заглавными буквами.
     */
    private boolean shout;

    /**
     * Включён ли стартер вообще.
     */
    private boolean enabled = true;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public GreeterProperties() {
        // нечего инициализировать
    }

    /**
     * Значение: шаблон.
     * @return Значение: шаблон
     */
    public String getTemplate() {
        return this.template;
    }

    /**
     * Новое значение: шаблон.
     * @param template Шаблон
     */
    public void setTemplate(final String template) {
        this.template = template;
    }

    /**
     * Значение: громкое сообщение.
     * @return Значение: громкое сообщение
     */
    public boolean isShout() {
        return this.shout;
    }

    /**
     * Новое значение: громкое сообщение.
     * @param shout Громкое сообщение
     */
    public void setShout(final boolean shout) {
        this.shout = shout;
    }

    /**
     * Значение свойства {@code enabled}.
     * @return Значение свойства {@code enabled}
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Новое значение свойства {@code enabled}.
     * @param enabled Значение {@code enabled}
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
