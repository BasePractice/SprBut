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
  * @param age Параметр типа
  * @param firstName Параметр типа
 * @since 1.0
 */
public record CustomerRecord(String id, String firstName, String lastName, int age, boolean vip) {

    /**
     * Изменение состояния у неизменяемого объекта — это создание нового.
     */
    public CustomerRecord withVip(final boolean newVip) {
        return new CustomerRecord(this.id, this.firstName, this.lastName, this.age, newVip);
    }

    /**
     * Компактный конструктор: валидация выполняется один раз, при создании.
     */
    public CustomerRecord {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id обязателен");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Возраст не может быть отрицательным: " + age);
        }
    }

    /**
     * Имя.
     * @return Имя
     */
    public String fullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }
}
