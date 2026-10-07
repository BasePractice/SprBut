/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Все учётки.
 * @since 1.0
 */
public interface Accounts {

    /**
     * Завести учётку с ролью {@link Role#USER}.
     * @param login    Уникальный логин
     * @param password Пароль в открытом виде
     * @return Новая учётка
     */
    Account add(String login, String password);

    /**
     * Учётка по идентификатору.
     * @param id Идентификатор
     * @return Учётка, если она есть
     */
    Optional<Account> account(UUID id);

    /**
     * Учётка, чьи логин и пароль совпали.
     * @param login    Логин
     * @param password Пароль в открытом виде
     * @return Учётка, если логин и пароль верны
     */
    Optional<Account> authenticated(String login, String password);
}
