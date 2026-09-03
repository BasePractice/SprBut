/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle HideUtilityClassConstructorCheck (30 lines)
package ru.sprbut.m23;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Слайд 222: точка входа сервиса, который живёт не один.
 *
 * <p>Всё, чем этот модуль отличается от предыдущих, начинается с допущения:
 * рядом есть другие приложения, они отвечают по сети, и любое из них может
 * не ответить. Дальше идут следствия — где брать адрес соседа, что делать
 * с его молчанием и как не уронить себя вслед за ним.</p>
 *
 * @since 1.0
 */
@SpringBootApplication
public class CloudApp {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public CloudApp() {
        // нечего инициализировать
    }

    /**
     * Точка входа.
     * @param args Аргументы командной строки
     */
    public static void main(final String... args) {
        SpringApplication.run(CloudApp.class, args);
    }
}
