/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.http.codec.ServerSentEvent;

/**
 * Уведомление, которое идёт через каналы Redis к экземплярам сервиса.
 * @since 1.0
 */
public interface Notice {

    /**
     * Уведомление в виде события SSE.
     * @return Событие
     */
    ServerSentEvent<String> sse();

    /**
     * Уведомление в виде JSON для канала.
     * @return JSON
     */
    String json();
}
