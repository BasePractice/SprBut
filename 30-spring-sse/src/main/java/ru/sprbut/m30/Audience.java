/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Пользователи, подключённые к этому экземпляру сервиса.
 * @since 1.0
 */
public interface Audience {

    /**
     * Поток событий пользователя, живущий, пока на него подписаны.
     * @param user Идентификатор пользователя
     * @return События
     */
    Flux<ServerSentEvent<String>> stream(long user);

    /**
     * Доставить событие одному пользователю, если он подключён.
     * @param user Идентификатор пользователя
     * @param event Событие
     */
    void deliver(long user, ServerSentEvent<String> event);

    /**
     * Доставить событие всем подключённым пользователям.
     * @param event Событие
     */
    void broadcast(ServerSentEvent<String> event);
}
