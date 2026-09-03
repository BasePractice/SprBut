/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle HideUtilityClassConstructorCheck (30 lines)
package ru.sprbut.m22;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Слайд 212: точка входа защищённого приложения.
 *
 * <p>Стартер {@code spring-boot-starter-security} меняет поведение
 * приложения одним фактом своего присутствия: без единой строки настройки
 * все эндпоинты закрываются, а в лог печатается сгенерированный пароль.
 * Безопасность по умолчанию включена, а не выключена.</p>
 *
 * @since 1.0
 */
@SpringBootApplication
public class SecurityApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SecurityApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(SecurityApp.class, args);
    }
}
