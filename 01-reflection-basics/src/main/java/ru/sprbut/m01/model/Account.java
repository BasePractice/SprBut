/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01.model;

import java.math.BigDecimal;

/**
 * Подопытный класс для примеров рефлексии.
 *
 * <p>Специально содержит поля и методы с разными модификаторами доступа,
 * статические и финальные члены — чтобы на нём было видно всё, что перечислено
 * на слайдах 3–10 презентации.</p>
 *
 * @since 1.0
 */
public class Account {

    /**
     * Значение {@code TYPE}.
     */
    public static final String TYPE = "CHECKING";

    /**
     * Идентификатор.
     */
    private final String id;
    /**
     * Владелец.
     */
    private String owner;
    /**
     * Баланс.
     */
    private BigDecimal balance;
    /**
     * Значение {@code blocked}.
     */
    protected boolean blocked;
    /**
     * Значение {@code cachedLabel}.
     */
    transient String cachedLabel;

    /**
     * Основной конструктор.
     * @param id Идентификатор
     * @param owner Владелец
     * @param balance Баланс
     */
    public Account(final String id, final String owner, final BigDecimal balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }

    /**
     * Значение свойства {@code id}.
     * @return Значение свойства {@code id}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Значение свойства {@code owner}.
     * @return Значение свойства {@code owner}
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Значение свойства {@code balance}.
     * @return Значение свойства {@code balance}
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * Значение свойства {@code blocked}.
     * @return Значение свойства {@code blocked}
     */
    public boolean isBlocked() {
        return this.blocked;
    }

    /**
     * Метод намеренно приватный: рефлексия умеет вызывать и такие.
     * @param fee Значение {@code fee}
     * @return Метод намеренно приватный: рефлексия умеет вызывать и такие
     */
    private BigDecimal applyFee(final BigDecimal fee) {
        this.balance = this.balance.subtract(fee);
        return this.balance;
    }

    private void block(final String reason) {
        this.blocked = true;
        this.cachedLabel = reason;
    }

    static String describeType() {
        return "Счёт типа " + TYPE;
    }
}
