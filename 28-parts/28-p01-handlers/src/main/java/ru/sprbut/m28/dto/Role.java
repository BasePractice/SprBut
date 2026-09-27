/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.dto;

/**
 * Роль пользователя.
 *
 * <p>Встроенный конвертер строки в перечисление ищет константу по
 * точному имени, и {@code ?role=admin} для него ошибка. Регистр
 * прощает {@code LenientEnumFactory}.</p>
 *
 * @since 1.0
 */
public enum Role {

    /**
     * Администратор.
     */
    ADMIN,

    /**
     * Обычный пользователь.
     */
    USER
}
