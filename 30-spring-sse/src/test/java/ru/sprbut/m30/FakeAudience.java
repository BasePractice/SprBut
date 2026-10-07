/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Пользователи, записывающие доставки строками «кому событие данные».
 * @since 1.0
 */
final class FakeAudience implements Audience {

    /**
     * Записанные доставки.
     */
    private final Collection<String> log;

    /**
     * Конструктор пустой записи.
     */
    FakeAudience() {
        this(new CopyOnWriteArrayList<>());
    }

    /**
     * Конструктор.
     * @param log Записанные доставки
     */
    FakeAudience(final Collection<String> log) {
        this.log = log;
    }

    @Override
    public Flux<ServerSentEvent<String>> stream(final long user) {
        return Flux.never();
    }

    @Override
    public void deliver(final long user, final ServerSentEvent<String> event) {
        this.log.add(String.format("%d %s %s", user, event.event(), event.data()));
    }

    @Override
    public void broadcast(final ServerSentEvent<String> event) {
        this.log.add(String.format("all %s %s", event.event(), event.data()));
    }

    /**
     * Записанные доставки.
     * @return Строки в порядке доставки
     */
    Collection<String> deliveries() {
        return this.log;
    }
}
