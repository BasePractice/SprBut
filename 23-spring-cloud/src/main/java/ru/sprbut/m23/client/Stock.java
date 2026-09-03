/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.client;

import java.util.function.Supplier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

/**
 * Слайд 226: отказ соседа не должен становиться отказом всего сервиса.
 *
 * <p>Предохранитель считает неудачи и, когда их доля переходит порог,
 * размыкает цепь: следующие вызовы не идут по сети вовсе, а сразу получают
 * запасной ответ. Смысл не в вежливости к соседу, а в себе — поток, ждущий
 * ответа от мёртвого сервиса, занят и не обслуживает никого.</p>
 *
 * <p>Запасной ответ — решение предметной области, а не техники: «ноль
 * на складе» безопаснее выдуманного числа, но опаснее честной ошибки, если
 * от этого числа зависит списание денег. Общего правильного ответа нет,
 * и потому его приходится выбирать здесь, а не в библиотеке.</p>
 *
 * @since 1.0
 */
@Service
public final class Stock {

    /**
     * Фабрика предохранителей.
     */
    private final CircuitBreakerFactory<?, ?> breakers;

    /**
     * Склад по ту сторону сети.
     */
    private final WarehouseApi warehouse;

    /**
     * Основной конструктор.
     * @param breakers Фабрика предохранителей
     * @param warehouse Склад по ту сторону сети
     */
    public Stock(final CircuitBreakerFactory<?, ?> breakers, final WarehouseApi warehouse) {
        this.breakers = breakers;
        this.warehouse = warehouse;
    }

    /**
     * Остаток товара или ноль, если склад молчит.
     * @param sku Артикул товара
     * @return Остаток товара или ноль, если склад молчит
     */
    public int of(final String sku) {
        return this.guarded(() -> this.warehouse.stock(sku));
    }

    /**
     * Тот же вызов под защитой, но с произвольным источником ответа.
     *
     * <p>Метод существует ради тестов: подменить сам источник проще
     * и честнее, чем поднимать соседа, который откажет по расписанию.</p>
     *
     * @param call Источник ответа
     * @return Ответ источника или ноль, если он не смог ответить
     */
    public int guarded(final Supplier<Integer> call) {
        return this.breakers.create("warehouse").run(call, failure -> 0);
    }
}
