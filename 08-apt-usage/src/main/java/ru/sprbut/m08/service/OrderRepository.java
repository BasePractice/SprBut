/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08.service;

import ru.sprbut.m07.api.Registered;
import ru.sprbut.m08.model.Order;
import java.util.ArrayList;
import java.util.List;

/**
 * Второй участник реестра. Имя не задано — процессор возьмёт имя класса
 * с маленькой буквы: {@code orderRepository}.
 * @since 1.0
 */
@Registered
public class OrderRepository {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public OrderRepository() {
        // нечего инициализировать
    }

    /**
     * Заказы.
     */
    private final List<Order> orders = new ArrayList<>();

    /**
     * Сохранение.
     * @param order Порядок
     * @return Сохранение
     */
    public Order save(final Order order) {
        this.orders.add(order);
        return order;
    }

    /**
     * Поиск по клиенту.
     * @param customerId Идентификатор
     * @return Поиск по клиенту
     */
    public List<Order> findByCustomer(final String customerId) {
        return this.orders.stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .toList();
    }

    /**
     * Количество.
     * @return Количество
     */
    public int count() {
        return this.orders.size();
    }
}
