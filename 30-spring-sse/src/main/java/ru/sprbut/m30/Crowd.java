/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Открытые потоки одного пользователя: общий источник событий, число подписчиков
 * и подписка на личный канал пользователя.
 *
 * <p>Число подписчиков ведётся самим объектом, а не берётся у источника: подключение
 * учитывается до подписки, и параллельный уход последнего подписчика не выбросит
 * источник, на который вот-вот подпишутся.</p>
 *
 * @since 1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
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
     * Подписка на личный канал.
     */
    private final Disposable link;

    /**
     * Конструктор толпы из одного подписчика без подписки на канал.
     */
    Crowd() {
        this(Sinks.many().multicast().directBestEffort(), 1, Disposables.disposed());
    }

    /**
     * Толпа, получающая события из канала.
     * @param source События личного канала
     * @return Новая толпа с тем же источником и подпиской на канал
     */
    Crowd attached(final Flux<ServerSentEvent<String>> source) {
        return new Crowd(this.sink, this.size, source.subscribe(this::emit));
    }

    /**
     * Толпа с ещё одним подписчиком.
     * @return Новая толпа с тем же источником
     */
    Crowd joined() {
        return new Crowd(this.sink, this.size + 1, this.link);
    }

    /**
     * Толпа без одного подписчика.
     * @return Новая толпа с тем же источником
     */
    Crowd left() {
        return new Crowd(this.sink, this.size - 1, this.link);
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
     *
     * <p>События приходят из личного канала и из общего, то есть из разных
     * потоков, поэтому одновременная отправка не теряется, а повторяется.</p>
     *
     * @param event Событие
     */
    void emit(final ServerSentEvent<String> event) {
        this.sink.emitNext(event, Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1L)));
    }

    /**
     * Отписаться от личного канала.
     */
    void close() {
        this.link.dispose();
    }
}
