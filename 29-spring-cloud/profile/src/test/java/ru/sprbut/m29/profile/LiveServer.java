/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import com.sun.net.httpserver.HttpServer;
import java.util.Locale;
import java.util.Map;

/**
 * Работающий тестовый HTTP-сервер, помнящий тела полученных запросов.
 * @since 1.0
 */
final class LiveServer implements AutoCloseable {

    /**
     * Сервер.
     */
    private final HttpServer server;

    /**
     * Тела полученных запросов по методу и пути.
     */
    private final Map<String, String> received;

    /**
     * Конструктор.
     * @param server   Сервер
     * @param received Тела полученных запросов по методу и пути
     */
    LiveServer(final HttpServer server, final Map<String, String> received) {
        this.server = server;
        this.received = received;
    }

    @Override
    public void close() {
        this.server.stop(0);
    }

    /**
     * Базовый адрес сервера.
     * @return Адрес
     */
    String url() {
        return String.format("http://127.0.0.1:%d", this.server.getAddress().getPort());
    }

    /**
     * Тело запроса, полученного методом по пути.
     * @param method HTTP-метод
     * @param path   Путь
     * @return Тело или пустая строка
     */
    String body(final String method, final String path) {
        return this.received.getOrDefault(String.format("%s %s", method, path), "");
    }

    /**
     * Был ли получен запрос методом по пути.
     * @param method HTTP-метод
     * @param path   Путь
     * @return Истина, если запрос был
     */
    boolean requested(final String method, final String path) {
        return this.received.containsKey(String.format("%s %s", method, path));
    }

    /**
     * Заголовок запроса, полученного методом по пути.
     * @param method HTTP-метод
     * @param path   Путь
     * @param name   Имя заголовка
     * @return Значение или пустая строка
     */
    String header(final String method, final String path, final String name) {
        return this.received.getOrDefault(
            String.format("%s %s #%s", method, path, name.toLowerCase(Locale.ROOT)), ""
        );
    }
}
