/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Альтернативная реализация — нужна, чтобы было что подменять.
 * @since 1.0
 */
public class SmsSender implements NotificationSender {

    /**
     * Отправленные сообщения.
     */
    private final List<String> sent = new ArrayList<>();

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SmsSender() {
        // нечего инициализировать
    }

    @Override
    public void send(final String recipient, final String message) {
        this.sent.add("SMS " + recipient + ": " + message);
    }

    @Override
    public String channel() {
        return "sms";
    }

    /**
     * Отправленные сообщения.
     * @return Отправленные сообщения
     */
    public List<String> sent() {
        return List.copyOf(this.sent);
    }
}
