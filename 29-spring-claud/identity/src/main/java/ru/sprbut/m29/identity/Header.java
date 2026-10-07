/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.identity;

/**
 * Заголовки, которыми gateway передаёт сервисам проверенного пользователя.
 * @since 1.0
 */
public enum Header {

    /**
     * Идентификатор пользователя.
     */
    ID("X-User-Id"),

    /**
     * Роль пользователя.
     */
    ROLE("X-User-Role"),

    /**
     * Логин пользователя.
     */
    LOGIN("X-User-Login");

    /**
     * Имя заголовка.
     */
    private final String title;

    /**
     * Конструктор.
     * @param title Имя заголовка
     */
    Header(final String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return this.title;
    }
}
