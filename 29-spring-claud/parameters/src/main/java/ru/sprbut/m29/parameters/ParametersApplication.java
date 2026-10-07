/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.parameters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Сервис, хранящий параметры владельцев по имени.
 * @since 1.0
 */
@SpringBootApplication(proxyBeanMethods = false)
public final class ParametersApplication {

    /**
     * Конструктор для Spring.
     */
    private ParametersApplication() {
    }

    /**
     * Запуск сервиса параметров.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(ParametersApplication.class, args);
    }
}
