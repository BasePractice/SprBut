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
 * Реализация «по умолчанию». Отправленное копится в списке — так его видно в тестах.
 * @since 1.0
 */
public class EmailSender implements NotificationSender {

    /**
     * Отправленные сообщения.
     */
    private final List<String> sent = new ArrayList<>(0);

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public EmailSender() {
        // нечего инициализировать
    }

    @Override
    public void send(final String recipient, final String message) {
        this.sent.add(recipient + " <- " + message);
    }

    @Override
    public String channel() {
        return "email";
    }

    /**
     * Отправленные сообщения.
     * @return Отправленные сообщения
     */
    public List<String> sent() {
        return List.copyOf(this.sent);
    }
}
