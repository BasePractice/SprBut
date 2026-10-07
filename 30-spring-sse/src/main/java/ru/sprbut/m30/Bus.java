/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m30;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Общий канал уведомлений между экземплярами сервиса.
 * @since 1.0
 */
public interface Bus {

    /**
     * Отправить уведомление всем экземплярам.
     * @param notice Уведомление
     * @return Завершение отправки
     */
    Mono<Void> publish(Notice notice);

    /**
     * Уведомления, приходящие из канала.
     * @return Бесконечный поток уведомлений
     */
    Flux<Notice> notices();
}
