/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ImplicitConstructorCheck disable
package ru.sprbut.m30;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Уведомление, пришедшее из канала в виде JSON и разбираемое при доставке.
 * @since 1.0
 */
@RequiredArgsConstructor
public final class ParsedNotice implements Notice {

    /**
     * Исходный JSON.
     */
    private final String text;

    /**
     * Разборщик JSON.
     */
    private final ObjectMapper mapper;

    @Override
    public ServerSentEvent<String> sse() {
        final JsonNode node = this.mapper.readTree(this.text);
        return new TextNotice(
            node.required("event").asString(), node.required("text").asString()
        ).sse();
    }

    @Override
    public String json() {
        return this.text;
    }
}
