/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle HideUtilityClassConstructorCheck (40 lines)
package ru.sprbut.m28;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа части p01: участники обработки запроса.
 *
 * <p>Приложение поднимает обычный Spring MVC (модуль 20) и расставляет
 * участников по всей дороге запроса туда и обратно: фильтры контейнера,
 * условия выбора метода, интерсепторы, конвертеры и форматтеры,
 * резолверы аргументов, советы над телами, обработчики результата и
 * исключений, аспект. Каждый вмешивается на своём уровне и видит ровно
 * то, что на этом уровне уже есть.</p>
 *
 * @since 1.0
 */
@SpringBootApplication
public final class HandlersApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public HandlersApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(HandlersApp.class, args);
    }
}
