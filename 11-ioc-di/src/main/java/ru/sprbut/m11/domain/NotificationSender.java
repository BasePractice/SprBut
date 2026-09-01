/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m11.domain;

/**
 * Абстракция, ради которой всё и затевается: пока код зависит от интерфейса,
 * его реализацию можно подменить — контейнером, фабрикой или руками в тесте.
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface NotificationSender {

    /**
     * Отправка.
     * @param recipient Значение {@code recipient}
     * @param message Сообщение
     */
    void send(String recipient, String message);

    /**
     * Канал.
     * @return Канал
     */
    String channel();
}
