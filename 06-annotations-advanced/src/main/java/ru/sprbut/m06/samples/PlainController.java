/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.samples;

import ru.sprbut.m06.web.Controller;

/**
 * Класс с прямой аннотацией — контрольный случай: здесь наивная проверка
 * тоже сработает.
 * @since 1.0
 */
@Controller
public class PlainController {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public PlainController() {
        // нечего инициализировать
    }
}
