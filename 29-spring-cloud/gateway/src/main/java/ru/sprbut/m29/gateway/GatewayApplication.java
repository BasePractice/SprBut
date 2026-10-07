/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Единая точка входа в систему.
 * @since 1.0
 */
@SpringBootApplication(proxyBeanMethods = false)
public final class GatewayApplication {

    /**
     * Конструктор для Spring.
     */
    private GatewayApplication() {
    }

    /**
     * Запуск gateway.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
