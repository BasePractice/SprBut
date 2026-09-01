/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// тема раздела — спецификация JavaBeans: имена свойств и одноимённые
// параметры сеттеров задаются ею, а не нашим стилем
// @checkstyle MemberNameCheck disable
// @checkstyle ParameterNameCheck disable
// @checkstyle HiddenFieldCheck disable
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
// бин намеренно состоит из одних свойств: раздел показывает,
// во что обходится шаблонный код классического JavaBean
@SuppressWarnings({"PMD.DataClass", "PMD.TooManyMethods"})
public class CustomerBean implements Serializable {

    /**
     * Версия для сериализации.
     */
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

    /**
     * Публичный конструктор без параметров — обязательное требование соглашения.
     */
    public CustomerBean() {
        // тело намеренно пустое
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
     * @return Полное имя, собранное из двух свойств
     */
    public String getFullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }

    @Override
    public final boolean equals(final Object other) {
        final boolean same;
        if (this == other) {
            same = true;
        } else if (other instanceof CustomerBean bean) {
            same = this.age == bean.age
                && this.vip == bean.vip
                && Objects.equals(this.id, bean.id)
                && Objects.equals(this.firstName, bean.firstName)
                && Objects.equals(this.lastName, bean.lastName);
        } else {
            same = false;
        }
        return same;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName, this.age, this.vip);
    }

    @Override
    public final String toString() {
        return String.format(
            "CustomerBean{id=%s, firstName=%s, lastName=%s, age=%s, vip=%s}",
            this.id, this.firstName, this.lastName, this.age, this.vip
        );
    }
}
