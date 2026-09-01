/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// модели маппинга: имена свойств и одноимённые параметры сеттеров —
// то, по чему маппер и находит соответствие
// @checkstyle MemberNameCheck disable
// @checkstyle HiddenFieldCheck disable
// @checkstyle ParameterNameCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09.model;

import java.util.Objects;

/**
 * Целевой объект маппинга. Поля {@code internalNote} здесь намеренно нет.
 * @since 1.0
 */
@SuppressWarnings("PMD.DataClass")
public final class UserDto {

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
    public boolean equals(final Object other) {
        final boolean same;
        if (this == other) {
            same = true;
        } else if (other instanceof UserDto dto) {
            same = this.age == dto.age
                && this.active == dto.active
                && Objects.equals(this.id, dto.id)
                && Objects.equals(this.firstName, dto.firstName)
                && Objects.equals(this.lastName, dto.lastName);
        } else {
            same = false;
        }
        return same;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName, this.age, this.active);
    }

    @Override
    public String toString() {
        return String.format(
            "UserDto{id=%s, firstName=%s, age=%s}", this.id, this.firstName, this.age
        );
    }
}
