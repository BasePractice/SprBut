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
 * @param event Имя события
 * @param text Текст
 * @since 1.0
 */
public record TextNotice(String event, String text) implements Notice {

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
