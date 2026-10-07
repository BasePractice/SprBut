/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Ответ тестового сервера: заданный статус, тело JSON по пути, запись тела и заголовков запроса.
 * @since 1.0
 */
final class FakeReply implements HttpHandler {

    /**
     * Статус ответа.
     */
    private final int status;

    /**
     * Тело ответа по пути запроса.
     */
    private final Function<String, String> answer;

    /**
     * Тела полученных запросов по методу и пути.
     */
    private final Map<String, String> received;

    /**
     * Конструктор.
     * @param status   Статус ответа
     * @param answer   Тело ответа по пути запроса
     * @param received Тела полученных запросов по методу и пути
     */
    FakeReply(
        final int status,
        final Function<String, String> answer,
        final Map<String, String> received
    ) {
        this.status = status;
        this.answer = answer;
        this.received = received;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final String path = exchange.getRequestURI().getPath();
        this.received.put(
            String.format("%s %s", exchange.getRequestMethod(), path),
            new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
        );
        exchange.getRequestHeaders().forEach(
            (name, values) -> this.received.put(
                String.format(
                    "%s %s #%s", exchange.getRequestMethod(), path, name.toLowerCase(Locale.ROOT)
                ),
                String.join(",", values)
            )
        );
        final byte[] body = this.answer.apply(path).getBytes(StandardCharsets.UTF_8);
        long length = -1;
        if (body.length > 0) {
            length = body.length;
        }
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(this.status, length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
