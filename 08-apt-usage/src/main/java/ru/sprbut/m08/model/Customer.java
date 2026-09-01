/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m08.model;

import ru.sprbut.m07.api.GenerateBuilder;
import java.math.BigDecimal;

/**
 * Обычный JavaBean, помеченный {@code @GenerateBuilder} (модуль 07).
 *
 * <p>Никакого билдера здесь не написано — он появится в
 * {@code target/generated-sources/annotations} на этапе компиляции,
 * и им можно пользоваться из обычного кода, как будто он написан руками.</p>
 *
 * @since 1.0
 */
@GenerateBuilder
public class Customer {
    /**
     * Идентификатор.
     */
    private String id;

    /**
     * Имя.
     */
    private String name;

    /**
     * Адрес почты.
     */
    private String email;

    /**
     * Возраст.
     */
    private int age;

    /**
     * Признак привилегированного клиента.
     */
    private boolean vip;

    /**
     * Баланс.
     */
    private BigDecimal balance;

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Customer() {
        // нечего инициализировать
    }

    /**
     * Значение свойства {@code id}.
     * @return Значение свойства {@code id}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Новое значение свойства {@code id}.
     * @param id Идентификатор
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Значение свойства {@code name}.
     * @return Значение свойства {@code name}
     */
    public String getName() {
        return this.name;
    }

    /**
     * Новое значение свойства {@code name}.
     * @param name Имя
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Значение свойства {@code email}.
     * @return Значение свойства {@code email}
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Новое значение свойства {@code email}.
     * @param email Адрес почты
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Значение свойства {@code age}.
     * @return Значение свойства {@code age}
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Новое значение свойства {@code age}.
     * @param age Возраст
     */
    public void setAge(final int age) {
        this.age = age;
    }

    /**
     * Значение: признак привилегированного клиента.
     * @return Значение: признак привилегированного клиента
     */
    public boolean isVip() {
        return this.vip;
    }

    /**
     * Новое значение: признак привилегированного клиента.
     * @param vip Признак привилегированного клиента
     */
    public void setVip(final boolean vip) {
        this.vip = vip;
    }

    /**
     * Значение свойства {@code balance}.
     * @return Значение свойства {@code balance}
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * Новое значение свойства {@code balance}.
     * @param balance Баланс
     */
    public void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Customer{this.id=" + this.id + ", this.name=" + this.name + ", this.vip=" + this.vip + "}";
    }
}
