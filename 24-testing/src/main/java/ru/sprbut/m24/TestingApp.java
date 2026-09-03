/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// класс приложения должен быть инстанцируемым: контейнер видит в нём
// конфигурацию, а не утилиту с одним main
// @checkstyle HideUtilityClassConstructorCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m24;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Небольшое приложение, на котором показываются все виды тестов Spring Boot.
 * @since 1.0
 */
@SpringBootApplication
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestingApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public TestingApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы
     */
    public static void main(final String[] args) {
        SpringApplication.run(TestingApp.class, args);
    }
}
