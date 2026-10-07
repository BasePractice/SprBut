/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.auth;

/**
 * Роль пользователя, определяющая его права.
 * @since 1.0
 */
public enum Role {

    /**
     * Обычный пользователь, распоряжается только собой.
     */
    USER,

    /**
     * Администратор, распоряжается всеми.
     */
    ADMIN
}
