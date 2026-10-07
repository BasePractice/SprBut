/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

/**
 * Уведомление, которое идёт через общий канал ко всем экземплярам сервиса.
 * @since 1.0
 */
public interface Notice {

    /**
     * Доставить уведомление тем пользователям, кому оно адресовано.
     * @param audience Подключённые пользователи
     */
    void deliver(Audience audience);

    /**
     * Уведомление в виде JSON для канала.
     * @return JSON
     */
    String json();
}
