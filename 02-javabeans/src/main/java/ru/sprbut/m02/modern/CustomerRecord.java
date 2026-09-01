/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.modern;

/**
 * Слайд 19: «Lombok, record, Immutability (Builder)» — ответ на избыточность
 * и мутабельность классического бина.
 *
 * <p>{@code record} даёт неизменяемость, {@code equals}/{@code hashCode}/{@code toString}
 * и компактный конструктор для валидации — в пяти строках вместо шестидесяти.</p>
 *
 * <p>Цена: record <b>не является</b> JavaBean. У него нет конструктора без параметров,
 * а аксессоры называются {@code firstName()}, а не {@code getFirstName()}. Поэтому
 * {@link java.beans.Introspector} не увидит у него ни одного свойства, и
 * старый код, рассчитанный на соглашение, с record работать не будет.</p>
 *
 * @param id Идентификатор
 * @param firstName Имя
 * @param lastName Фамилия
 * @param age Возраст
 * @param vip Признак привилегированного клиента
 * @since 1.0
 */
public record CustomerRecord(
    String id, String firstName, String lastName, int age, boolean vip
) {

    /**
     * Компактный конструктор: валидация выполняется один раз, при создании.
     */
    public CustomerRecord {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id обязателен");
        }
        if (age < 0) {
            throw new IllegalArgumentException(
                String.format("Возраст не может быть отрицательным: %d", age)
            );
        }
    }

    /**
     * Изменение состояния у неизменяемого объекта — это создание нового.
     * @param vip Новый признак привилегированного клиента
     * @return Новый объект с изменённым признаком
     * @checkstyle HiddenFieldCheck (3 lines)
     */
    public CustomerRecord withVip(final boolean vip) {
        return new CustomerRecord(this.id, this.firstName, this.lastName, this.age, vip);
    }

    /**
     * Имя.
     * @return Имя
     */
    public String fullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }
}
