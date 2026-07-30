package ru.sprbut.m01.model;

import java.math.BigDecimal;

/**
 * Подопытный класс для примеров рефлексии.
 * <p>
 * Специально содержит поля и методы с разными модификаторами доступа,
 * статические и финальные члены — чтобы на нём было видно всё, что перечислено
 * на слайдах 3–10 презентации.
 */
public class Account {

    public static final String TYPE = "CHECKING";

    private final String id;
    private String owner;
    private BigDecimal balance;
    protected boolean blocked;
    transient String cachedLabel;

    public Account(String id, String owner, BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    /**
     * Метод намеренно приватный: рефлексия умеет вызывать и такие.
     */
    private BigDecimal applyFee(BigDecimal fee) {
        this.balance = this.balance.subtract(fee);
        return this.balance;
    }

    private void block(String reason) {
        this.blocked = true;
        this.cachedLabel = reason;
    }

    static String describeType() {
        return "Счёт типа " + TYPE;
    }
}
