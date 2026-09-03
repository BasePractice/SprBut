/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle HideUtilityClassConstructorCheck (30 lines)
package ru.sprbut.m20;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Слайд 193: точка входа веб-приложения.
 *
 * <p>Одна аннотация превращает обычный main-класс в веб-сервер: стартер
 * {@code spring-boot-starter-web} приносит Tomcat и Spring MVC, а
 * автоконфигурация (модуль 19) поднимает {@code DispatcherServlet}
 * и регистрирует его на «/».</p>
 *
 * @since 1.0
 */
@SpringBootApplication
public class MvcApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public MvcApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(MvcApp.class, args);
    }
}
