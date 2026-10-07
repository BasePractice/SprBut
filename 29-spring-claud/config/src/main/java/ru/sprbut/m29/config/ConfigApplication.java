/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Сервер, раздающий сервисам их настройки из файлов в classpath.
 * @since 1.0
 */
@SpringBootApplication(proxyBeanMethods = false)
@EnableConfigServer
public final class ConfigApplication {

    /**
     * Конструктор для Spring.
     */
    private ConfigApplication() {
    }

    /**
     * Запуск сервера настроек.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(ConfigApplication.class, args);
    }
}
