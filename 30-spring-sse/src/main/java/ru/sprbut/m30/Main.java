/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Сервис, доставляющий события пользователям по SSE через общий канал Redis.
 * @since 1.0
 */
@SpringBootApplication(proxyBeanMethods = false)
public final class Main {

    /**
     * Конструктор для Spring.
     */
    private Main() {
    }

    /**
     * Запуск сервиса.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(Main.class, args);
    }
}
