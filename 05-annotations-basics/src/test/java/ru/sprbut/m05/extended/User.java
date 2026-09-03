/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

/**
 * Подопытный объект со всеми видами ограничений сразу.
 *
 * <p>Наследуется от {@link BaseEntity} намеренно: ограничения родителя должны
 * действовать, хотя {@code @Inherited} к полям отношения не имеет.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public final class User extends BaseEntity {

    /**
     * Логин.
     */
    @NotBlank
    @MaxLength(10)
    private final String login;

    /**
     * Возраст.
     */
    @Range(min = 18, max = 120, message = "возраст вне диапазона")
    private final int age;

    /**
     * Адрес почты.
     */
    @Matches(regex = ".*@.*", message = "не похоже на почту")
    @Matches(regex = ".*\\.[a-z]+", message = "нет доменной зоны")
    private final String email;

    /**
     * Невидимый элемент.
     */
    @InvisibleNotNull
    private final String invisible;

    /**
     * Значение {@code free}.
     */
    private final String free;

    /**
     * Основной конструктор.
     * @param id Идентификатор
     * @param login Логин
     * @param age Возраст
     * @param email Адрес почты
     */
    public User(final String id, final String login, final int age, final String email) {
        this(id, login, age, email, null, "без ограничений");
    }

    /**
     * Основной конструктор.
     * @param id Идентификатор
     * @param login Логин
     * @param age Возраст
     * @param email Адрес почты
     * @param invisible Невидимый элемент
     * @param free Значение {@code free}
     * @checkstyle ParameterNumberCheck (8 lines)
     */
    public User(final String id, final String login, final int age,
        final String email, final String invisible, final String free) {
        super(id);
        this.login = login;
        this.age = age;
        this.email = email;
        this.invisible = invisible;
        this.free = free;
    }
}
