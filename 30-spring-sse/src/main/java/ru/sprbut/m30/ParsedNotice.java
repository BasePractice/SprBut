/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Уведомление, пришедшее из канала в виде JSON и разбираемое при доставке.
 * @since 1.0
 */
public final class ParsedNotice implements Notice {

    /**
     * Исходный JSON.
     */
    private final String text;

    /**
     * Разборщик JSON.
     */
    private final ObjectMapper mapper;

    /**
     * Конструктор.
     * @param text Исходный JSON
     * @param mapper Разборщик JSON
     */
    public ParsedNotice(final String text, final ObjectMapper mapper) {
        this.text = text;
        this.mapper = mapper;
    }

    @Override
    public void deliver(final Audience audience) {
        this.origin().deliver(audience);
    }

    @Override
    public String json() {
        return this.text;
    }

    private Notice origin() {
        final JsonNode node = this.mapper.readTree(this.text);
        final String type = node.required("type").asString();
        final Notice notice;
        if ("USER".equals(type)) {
            notice = new UserNotice(
                node.required("userId").asLong(), node.required("text").asString()
            );
        } else if ("BROADCAST".equals(type)) {
            notice = new BroadcastNotice(node.required("text").asString());
        } else {
            throw new IllegalArgumentException(
                String.format("Тип уведомления '%s' неизвестен в %s", type, this.text)
            );
        }
        return notice;
    }
}
