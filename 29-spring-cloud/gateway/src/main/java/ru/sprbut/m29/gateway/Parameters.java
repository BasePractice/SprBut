/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.gateway;

import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Характеристики пользователей во внутреннем сервисе parameters.
 * @since 1.0
 */
@FunctionalInterface
public interface Parameters {

    /**
     * Все характеристики пользователя.
     * @param owner Идентификатор пользователя
     * @return Значения по именам
     */
    Mono<Map<String, String>> all(String owner);
}
