/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

/**
 * Ошибка выполнения самой команды — в отличие от ошибки её разбора.
 * @since 1.0
 */
public final class CommandFailed extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Основной конструктор.
     * @param message Сообщение
     * @param cause Причина
     */
    public CommandFailed(final String message, final Throwable cause) {
        super(message, cause);
    }
}
