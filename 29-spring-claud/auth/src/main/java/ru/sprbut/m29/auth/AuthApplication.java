/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Сервис учёток и токенов.
 * @since 1.0
 */
@SpringBootApplication(proxyBeanMethods = false)
public final class AuthApplication {

    /**
     * Конструктор для Spring.
     */
    private AuthApplication() {
    }

    /**
     * Запуск сервиса.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
