/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.http.codec.ServerSentEvent;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Уведомление всем подключённым пользователям.
 * @since 1.0
 */
public final class BroadcastNotice implements Notice {

    /**
     * Текст.
     */
    private final String text;

    /**
     * Конструктор.
     * @param text Текст
     */
    public BroadcastNotice(final String text) {
        this.text = text;
    }

    @Override
    public void deliver(final Audience audience) {
        audience.broadcast(
            ServerSentEvent.<String>builder().event("broadcast").data(this.text).build()
        );
    }

    @Override
    public String json() {
        return JsonNodeFactory.instance.objectNode()
            .put("type", "BROADCAST")
            .put("text", this.text)
            .toString();
    }
}
