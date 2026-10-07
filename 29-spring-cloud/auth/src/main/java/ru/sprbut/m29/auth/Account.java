/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.Map;
import java.util.UUID;

/**
 * Учётка пользователя: логин, пароль и роль.
 * @since 1.0
 */
public interface Account {

    /**
     * Идентификатор.
     * @return Идентификатор
     */
    UUID id();

    /**
     * Логин.
     * @return Логин
     */
    String login();

    /**
     * Роль.
     * @return Роль
     */
    Role role();

    /**
     * Представление для ответа клиенту, без пароля.
     * @return Поля по именам
     */
    Map<String, Object> json();

    /**
     * Сменить пароль.
     * @param password Новый пароль в открытом виде
     */
    void password(String password);

    /**
     * Назначить роль.
     * @param role Новая роль
     */
    void grant(Role role);

    /**
     * Удалить учётку вместе с её сессиями.
     */
    void delete();
}
