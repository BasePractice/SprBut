package ru.sprbut.m03.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Подопытный класс для карты Reflection API (СХЕМА 1, слайд 27).
 * Содержит разные конструкторы, дженерики, varargs и throws —
 * всё то, что умеют разбирать {@code Field}, {@code Method} и {@code Constructor}.
 */
public class Order {

    public static final String STATUS_NEW = "NEW";

    private final String id;
    private String customer;
    private BigDecimal total;
    private List<String> items;
    private Map<String, BigDecimal> discounts;
    private volatile boolean paid;

    public Order() {
        this("UNKNOWN");
    }

    public Order(String id) {
        this(id, BigDecimal.ZERO);
    }

    public Order(String id, BigDecimal total) {
        this.id = id;
        this.total = total;
    }

    protected Order(String id, String customer, BigDecimal total) {
        this.id = id;
        this.customer = customer;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<String> getItems() {
        return items;
    }

    public Map<String, BigDecimal> getDiscounts() {
        return discounts;
    }

    public boolean isPaid() {
        return paid;
    }

    /** Метод с varargs — в рефлексии это параметр-массив плюс флаг {@code isVarArgs}. */
    public BigDecimal addLines(BigDecimal... amounts) {
        BigDecimal sum = total == null ? BigDecimal.ZERO : total;
        for (BigDecimal amount : amounts) {
            sum = sum.add(amount);
        }
        this.total = sum;
        return sum;
    }

    /** Метод с объявленным checked-исключением. */
    public void pay(BigDecimal amount) throws PaymentException {
        if (total == null || amount.compareTo(total) < 0) {
            throw new PaymentException("Недостаточная сумма: " + amount + " < " + total);
        }
        this.paid = true;
    }

    /** Синхронизированный метод — ещё один флаг в {@code Modifier}. */
    public synchronized void cancel() {
        this.paid = false;
        this.total = BigDecimal.ZERO;
    }

    private String internalTag() {
        return "order-" + id;
    }

    public static class PaymentException extends Exception {
        public PaymentException(String message) {
            super(message);
        }
    }
}
