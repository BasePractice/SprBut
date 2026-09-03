/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.client;

import org.springframework.web.service.annotation.GetExchange;

/**
 * Слайд 225: декларативный клиент — интерфейс вместо кода запроса.
 *
 * <p>Реализации у этого интерфейса нет и не будет: её соберёт
 * {@code HttpServiceProxyFactory} — тем же динамическим прокси, который
 * разбирался в модуле 04. Аннотация {@code @GetExchange} для клиента значит
 * ровно то же, что {@code @GetMapping} для сервера, только читается
 * с другой стороны провода.</p>
 *
 * <p>Спор «Feign или HTTP Interface» на этом и заканчивается: с приходом
 * интерфейсов в сам Spring отдельная библиотека для той же идеи перестала
 * быть нужна.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface WarehouseApi {

    /**
     * Остаток товара на складе.
     * @param sku Артикул товара
     * @return Остаток товара на складе
     */
    @GetExchange("/stock/{sku}")
    int stock(String sku);
}
