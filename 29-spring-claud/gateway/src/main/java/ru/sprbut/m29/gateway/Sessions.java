/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import reactor.core.publisher.Mono;

/**
 * Сессии токенов в сервисе auth.
 * @since 1.0
 */
@FunctionalInterface
public interface Sessions {

    /**
     * Активна ли сессия.
     * @param sid Идентификатор сессии из claim {@code sid}
     * @return Истина, если сессия не закрыта и не истекла
     */
    Mono<Boolean> active(String sid);
}
