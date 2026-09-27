/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.routing;

import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Подмена таблицы маршрутов своей.
 *
 * <p>{@code WebMvcConfigurer} настраивает то, что создал Spring;
 * {@code WebMvcRegistrations} позволяет создать это самому. Boot
 * спрашивает у бина этого типа, чем заменить стандартную таблицу
 * маршрутов, адаптер вызова методов или обработчик исключений, и
 * настраивает подменённый объект так же, как настроил бы свой:
 * версии API, пути и интерсепторы продолжают работать.</p>
 *
 * @since 1.0
 */
@Component
public final class ClientRegistrations implements WebMvcRegistrations {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ClientRegistrations() {
        // нечего инициализировать
    }

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ClientHandlerMapping();
    }
}
