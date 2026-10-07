/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Открытые потоки одного пользователя: общий источник событий и число подписчиков.
 *
 * <p>Число подписчиков ведётся самим объектом, а не берётся у источника: подключение
 * учитывается до подписки, и параллельный уход последнего подписчика не выбросит
 * источник, на который вот-вот подпишутся.</p>
 *
 * @since 1.0
 */
final class Crowd {

    /**
     * Источник событий.
     */
    private final Sinks.Many<ServerSentEvent<String>> sink;

    /**
     * Число подписчиков.
     */
    private final int size;

    /**
     * Конструктор толпы из одного подписчика.
     */
    Crowd() {
        this(Sinks.many().multicast().directBestEffort(), 1);
    }

    /**
     * Конструктор.
     * @param sink Источник событий
     * @param size Число подписчиков
     */
    Crowd(final Sinks.Many<ServerSentEvent<String>> sink, final int size) {
        this.sink = sink;
        this.size = size;
    }

    /**
     * Толпа с ещё одним подписчиком.
     * @return Новая толпа с тем же источником
     */
    Crowd joined() {
        return new Crowd(this.sink, this.size + 1);
    }

    /**
     * Толпа без одного подписчика.
     * @return Новая толпа с тем же источником
     */
    Crowd left() {
        return new Crowd(this.sink, this.size - 1);
    }

    /**
     * Остался ли последний подписчик.
     * @return Истина, если подписчик один
     */
    boolean last() {
        return this.size == 1;
    }

    /**
     * События толпы.
     * @return Поток событий
     */
    Flux<ServerSentEvent<String>> flux() {
        return this.sink.asFlux();
    }

    /**
     * Отправить событие всем подписчикам толпы.
     * @param event Событие
     */
    void emit(final ServerSentEvent<String> event) {
        this.sink.tryEmitNext(event);
    }
}
