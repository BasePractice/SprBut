/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Каналы уведомлений между экземплярами сервиса: личный канал на каждого
 * пользователя и общий канал на всех.
 * @since 1.0
 */
public interface Bus {

    /**
     * Опубликовать уведомление в личный канал пользователя.
     * @param user Идентификатор пользователя
     * @param notice Уведомление
     * @return Сколько экземпляров подписаны на канал пользователя
     */
    Mono<Long> publish(long user, Notice notice);

    /**
     * Опубликовать уведомление в общий канал.
     * @param notice Уведомление
     * @return Сколько экземпляров подписаны на общий канал
     */
    Mono<Long> publish(Notice notice);

    /**
     * События из личного канала пользователя: подписка на поток подписывает
     * экземпляр на канал, отмена — отписывает.
     * @param user Идентификатор пользователя
     * @return Бесконечный поток событий
     */
    Flux<ServerSentEvent<String>> user(long user);

    /**
     * События из общего канала.
     * @return Бесконечный поток событий
     */
    Flux<ServerSentEvent<String>> broadcasts();
}
