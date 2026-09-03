/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle HideUtilityClassConstructorCheck (30 lines)
package ru.sprbut.m21;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Слайд 203: точка входа реактивного приложения.
 *
 * <p>Аннотация та же, что в модуле 20, а сервер другой: стартер
 * {@code spring-boot-starter-webflux} приносит Netty вместо Tomcat, и
 * {@code DispatcherServlet} не появляется вовсе — Servlet API здесь
 * не участвует.</p>
 *
 * @since 1.0
 */
@SpringBootApplication
public class QuotesApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public QuotesApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(QuotesApp.class, args);
    }
}
