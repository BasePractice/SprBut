/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.samples;

import ru.sprbut.m06.web.RestController;

/**
 * Класс с композицией первого уровня: {@code @Controller} доступен через
 * {@code @RestController}, но не напрямую.
 * @since 1.0
 */
@RestController("users")
public class UserApi {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public UserApi() {
        // нечего инициализировать
    }
}
