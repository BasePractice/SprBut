/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m16;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Точка входа модуля. {@code @ConfigurationPropertiesScan} находит классы
 * с {@code @ConfigurationProperties} — без него их пришлось бы регистрировать
 * вручную через {@code @EnableConfigurationProperties}.
 * @since 1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigurationApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ConfigurationApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы
     */
    public static void main(final String[] args) {
        SpringApplication.run(ConfigurationApp.class, args);
    }
}
