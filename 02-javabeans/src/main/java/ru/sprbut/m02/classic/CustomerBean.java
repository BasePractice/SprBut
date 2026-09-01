/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.io.Serializable;
import java.util.Objects;

/**
 * Слайды 12–17: классический JavaBean по всем правилам соглашения.
 * <ul>
 * <li>публичный конструктор без параметров;</li>
 * <li>свойства доступны через getter/setter;</li>
 * <li>реализует {@link Serializable} (Spring этого не требует, но соглашение — да).</li>
 * </ul>
 *
 * <p>Обратная сторона, о которой говорит слайд 18: <b>избыточность и мутабельность</b>.
 * Пять свойств — это ~60 строк шаблонного кода, и объект в любой момент можно
 * привести в невалидное состояние: он создаётся пустым и заполняется по частям.</p>
 *
 * @since 1.0
 */
public class CustomerBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Идентификатор.
     */
    private String id;
    /**
     * Имя.
     */
    private String firstName;
    /**
     * Имя.
     */
    private String lastName;
    /**
     * Возраст.
     */
    private int age;
    /**
     * Признак привилегированного клиента.
     */
    private boolean vip;

    /** Публичный конструктор без параметров — обязательное требование соглашения. */
    public CustomerBean() {
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
     * Значение: имя.
     * @return Значение: имя
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Новое значение: имя.
     * @param firstName Имя
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * Значение: фамилия.
     * @return Значение: фамилия
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Новое значение: фамилия.
     * @param lastName Имя
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
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
     * Для boolean соглашение разрешает префикс {@code is} вместо {@code get}.
     * {@link java.beans.Introspector} понимает оба варианта.
     * @return Для boolean соглашение разрешает префикс {@code is} вместо {@code get}
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
     * Вычисляемое свойство: геттер без поля. Introspector всё равно увидит
     * свойство {@code fullName} — свойство определяется методами, а не полями.
     * @return Вычисляемое свойство: геттер без поля. Introspector всё равно увидит свойство {@code fullName} — свойство определяется методами, а не полями
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomerBean other)) {
            return false;
        }
        return this.age == other.age
                && this.vip == other.vip
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.firstName, other.firstName)
                && Objects.equals(this.lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName, this.age, this.vip);
    }

    @Override
    public String toString() {
        return "CustomerBean{this.id=" + this.id + ", this.firstName=" + this.firstName + ", this.lastName=" + this.lastName
                + ", this.age=" + this.age + ", this.vip=" + this.vip + "}";
    }
}
