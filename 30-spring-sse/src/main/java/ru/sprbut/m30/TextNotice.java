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
 * Уведомление из имени события и текста.
 * @since 1.0
 */
public final class TextNotice implements Notice {

    /**
     * Имя события.
     */
    private final String event;

    /**
     * Текст.
     */
    private final String text;

    /**
     * Конструктор.
     * @param event Имя события
     * @param text Текст
     */
    public TextNotice(final String event, final String text) {
        this.event = event;
        this.text = text;
    }

    @Override
    public ServerSentEvent<String> sse() {
        return ServerSentEvent.<String>builder().event(this.event).data(this.text).build();
    }

    @Override
    public String json() {
        return JsonNodeFactory.instance.objectNode()
            .put("event", this.event)
            .put("text", this.text)
            .toString();
    }
}
