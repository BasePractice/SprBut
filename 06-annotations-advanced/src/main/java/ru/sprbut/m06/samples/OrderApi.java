/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.samples;

import ru.sprbut.m06.web.ApiController;

/**
 * Класс с композицией второго уровня — до {@code @Controller} отсюда
 * два шага мета-аннотаций.
 * @since 1.0
 */
@ApiController("orders")
public class OrderApi {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public OrderApi() {
        // нечего инициализировать
    }
}
