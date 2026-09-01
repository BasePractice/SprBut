/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09.model;

import java.util.Objects;

/**
 * Целевой объект маппинга. Поля {@code internalNote} здесь намеренно нет.
 * @since 1.0
 */
public class UserDto {

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
     * Признак активности.
     */
    private boolean active;

    /**
     * Основной конструктор.
     */
    public UserDto() {
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
     * Значение свойства {@code active}.
     * @return Значение свойства {@code active}
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Новое значение свойства {@code active}.
     * @param active Признак активности
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDto other)) {
            return false;
        }
        return this.age == other.age && this.active == other.active
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.firstName, other.firstName)
                && Objects.equals(this.lastName, other.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName, this.age, this.active);
    }

    @Override
    public String toString() {
        return "UserDto{this.id=" + this.id + ", this.firstName=" + this.firstName + ", this.age=" + this.age + "}";
    }
}
