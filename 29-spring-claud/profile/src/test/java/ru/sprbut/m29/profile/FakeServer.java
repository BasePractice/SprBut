/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * HTTP-сервер для тестов, отвечающий заданным статусом и телом по пути запроса.
 * @since 1.0
 */
final class FakeServer {

    /**
     * Статус ответа.
     */
    private final int status;

    /**
     * Тело ответа по пути запроса.
     */
    private final Function<String, String> answer;

    /**
     * Конструктор.
     * @param status Статус ответа
     * @param answer Тело ответа по пути запроса
     */
    FakeServer(final int status, final Function<String, String> answer) {
        this.status = status;
        this.answer = answer;
    }

    /**
     * Запустить сервер на эфемерном порту петлевого интерфейса.
     * @return Работающий сервер
     * @throws IOException Если порт не открылся
     */
    LiveServer started() throws IOException {
        final Map<String, String> received = new ConcurrentHashMap<>(1);
        final HttpServer server = HttpServer.create(
            new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0
        );
        server.createContext("/", new FakeReply(this.status, this.answer, received));
        server.start();
        return new LiveServer(server, received);
    }
}
