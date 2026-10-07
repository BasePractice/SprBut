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
 * Уведомление одному пользователю.
 * @since 1.0
 */
public final class UserNotice implements Notice {

    /**
     * Идентификатор пользователя.
     */
    private final long user;

    /**
     * Текст.
     */
    private final String text;

    /**
     * Конструктор.
     * @param user Идентификатор пользователя
     * @param text Текст
     */
    public UserNotice(final long user, final String text) {
        this.user = user;
        this.text = text;
    }

    @Override
    public void deliver(final Audience audience) {
        audience.deliver(
            this.user,
            ServerSentEvent.<String>builder().event("user-event").data(this.text).build()
        );
    }

    @Override
    public String json() {
        return JsonNodeFactory.instance.objectNode()
            .put("type", "USER")
            .put("userId", this.user)
            .put("text", this.text)
            .toString();
    }
}
