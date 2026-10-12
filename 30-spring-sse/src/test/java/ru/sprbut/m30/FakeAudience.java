/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * Пользователи, записывающие общие рассылки строками «all событие данные».
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class FakeAudience implements Audience {

    /**
     * Записанные рассылки.
     */
    private final Collection<String> log;

    /**
     * Конструктор пустой записи.
     */
    FakeAudience() {
        this(new CopyOnWriteArrayList<>());
    }

    @Override
    public Flux<ServerSentEvent<String>> stream(final long user) {
        return Flux.never();
    }

    @Override
    public void broadcast(final ServerSentEvent<String> event) {
        this.log.add(String.format("all %s %s", event.event(), event.data()));
    }

    /**
     * Записанные рассылки.
     * @return Строки в порядке рассылки
     */
    Collection<String> deliveries() {
        return this.log;
    }
}
