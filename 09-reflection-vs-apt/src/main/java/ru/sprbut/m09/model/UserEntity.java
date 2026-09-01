/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09.model;

/**
 * Источник для маппинга. Один и тот же перенос данных в {@link UserDto}
 * реализован в модуле тремя способами — рефлексией, «как после APT» и байткодом.
 * @since 1.0
 */
public class UserEntity {

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
     * Внутренняя заметка.
     */
    private String internalNote;

    /**
     * Основной конструктор.
     */
    public UserEntity() {
    }

    /**
     * Основной конструктор.
     * @param id Идентификатор
     * @param firstName Имя
     * @param lastName Имя
     * @param age Возраст
     * @param active Признак активности
     */
    public UserEntity(final String id, final String firstName, final String lastName, final int age, final boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.active = active;
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

    /**
     * Значение: внутренняя заметка.
     * @return Значение: внутренняя заметка
     */
    public String getInternalNote() {
        return this.internalNote;
    }

    /**
     * Новое значение: внутренняя заметка.
     * @param internalNote Внутренняя заметка
     */
    public void setInternalNote(final String internalNote) {
        this.internalNote = internalNote;
    }
}
