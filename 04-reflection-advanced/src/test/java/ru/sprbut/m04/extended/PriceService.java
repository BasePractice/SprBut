/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04.extended;

/**
 * Контракт, вокруг которого строится мини-AOP в тестах.
 *
 * <p>Аннотаций здесь нет намеренно: они висят на реализации, и это принципиально —
 * прокси обязан искать метаданные там, а не на интерфейсе.</p>
 *
 * @since 1.0
 */
public interface PriceService {

    /**
     * Цена товара — кэшируемая операция.
     * @param sku Артикул
     * @return Цена товара — кэшируемая операция
     */
    int price(String sku);

    /**
     * Нестабильная операция, которую имеет смысл повторять.
     * @return Нестабильная операция, которую имеет смысл повторять
     */
    int flaky();

    /**
     * Операция, полностью подменяемая заглушкой.
     * @return Операция, полностью подменяемая заглушкой
     */
    String currency();

    /**
     * Операция без единой аннотации.
     * @param value Значение
     * @return Операция без единой аннотации
     */
    int plain(int value);

    /**
     * Двойной запрос цены — через вызов соседнего метода внутри объекта.
     * @param sku Артикул
     * @return Двойной запрос цены — через вызов соседнего метода внутри объекта
     */
    int priceTwice(String sku);
}
