package ru.sprbut.m08.service;

import ru.sprbut.m07.api.Registered;
import ru.sprbut.m08.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Второй участник реестра. Имя не задано — процессор возьмёт имя класса
 * с маленькой буквы: {@code orderRepository}.
 */
@Registered
public class OrderRepository {

    private final List<Order> orders = new ArrayList<>();

    public Order save(Order order) {
        orders.add(order);
        return order;
    }

    public List<Order> findByCustomer(String customerId) {
        return orders.stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .toList();
    }

    public int count() {
        return orders.size();
    }
}
