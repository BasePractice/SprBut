/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Подопытный класс для карты Reflection API (СХЕМА 1, слайд 27).
 * Содержит разные конструкторы, дженерики, varargs и throws —
 * всё то, что умеют разбирать {@code Field}, {@code Method} и {@code Constructor}.
 * @since 1.0
 */
public class Order {

    /**
     * Значение {@code STATUS_NEW}.
     */
    public static final String STATUS_NEW = "NEW";

    /**
     * Идентификатор.
     */
    private final String id;
    /**
     * Клиент.
     */
    private String customer;
    /**
     * Итоговая сумма.
     */
    private BigDecimal total;
    /**
     * Значение {@code items}.
     */
    private List<String> items;
    /**
     * Значение {@code discounts}.
     */
    private Map<String, BigDecimal> discounts;
    /**
     * Значение {@code paid}.
     */
    private volatile boolean paid;

    /**
     * Основной конструктор.
     */
    public Order() {
        this("UNKNOWN");
    }

    /**
     * Основной конструктор.
     * @param id Идентификатор
     */
    public Order(final String id) {
        this(id, BigDecimal.ZERO);
    }

    /**
     * Основной конструктор.
     * @param id Идентификатор
     * @param total Итоговая сумма
     */
    public Order(final String id, final BigDecimal total) {
        this.id = id;
        this.total = total;
    }

    protected Order(final String id, final String customer, final BigDecimal total) {
        this.id = id;
        this.customer = customer;
        this.total = total;
    }

    /**
     * Значение свойства {@code id}.
     * @return Значение свойства {@code id}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Значение: клиент.
     * @return Значение: клиент
     */
    public String getCustomer() {
        return this.customer;
    }

    /**
     * Новое значение: клиент.
     * @param customer Клиент
     */
    public void setCustomer(final String customer) {
        this.customer = customer;
    }

    /**
     * Значение: итоговая сумма.
     * @return Значение: итоговая сумма
     */
    public BigDecimal getTotal() {
        return this.total;
    }

    /**
     * Значение свойства {@code items}.
     * @return Значение свойства {@code items}
     */
    public List<String> getItems() {
        return this.items;
    }

    /**
     * Значение свойства {@code discounts}.
     * @return Значение свойства {@code discounts}
     */
    public Map<String, BigDecimal> getDiscounts() {
        return this.discounts;
    }

    /**
     * Значение свойства {@code paid}.
     * @return Значение свойства {@code paid}
     */
    public boolean isPaid() {
        return this.paid;
    }

    /**
     * Метод с varargs — в рефлексии это параметр-массив плюс флаг {@code isVarArgs}.
     * @param amounts Значение {@code amounts}
     * @return Метод с varargs — в рефлексии это параметр-массив плюс флаг {@code isVarArgs}
     */
    public BigDecimal addLines(final BigDecimal... amounts) {
        BigDecimal sum = this.total == null ? BigDecimal.ZERO : this.total;
        for (BigDecimal amount : amounts) {
            sum = sum.add(amount);
        }
        this.total = sum;
        return sum;
    }

    /**
     * Метод с объявленным checked-исключением.
     * @param amount Сумма
     */
    public void pay(final BigDecimal amount) throws PaymentException {
        if (this.total == null || amount.compareTo(this.total) < 0) {
            throw new PaymentException("Недостаточная сумма: " + amount + " < " + this.total);
        }
        this.paid = true;
    }

    /**
     * Синхронизированный метод — ещё один флаг в {@code Modifier}.
     */
    public synchronized void cancel() {
        this.paid = false;
        this.total = BigDecimal.ZERO;
    }

    private String internalTag() {
        return "order-" + this.id;
    }

    /**
     * Значение {@code PaymentException}.
     */
    public static class PaymentException extends Exception {
        /**
         * Основной конструктор.
         * @param message Сообщение
         */
        public PaymentException(final String message) {
            super(message);
        }
    }
}
