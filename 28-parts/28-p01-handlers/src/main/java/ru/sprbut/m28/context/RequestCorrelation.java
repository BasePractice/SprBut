/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.context;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Идентификатор одного конкретного запроса.
 *
 * @param id Идентификатор
 * @since 1.0
 */
public record RequestCorrelation(@NonNull String id) implements Correlation {

    /**
     * Компактный конструктор: запрос без идентификатора не бывает.
     * @param id Идентификатор
     */
    public RequestCorrelation {
        Objects.requireNonNull(id, "у запроса нет идентификатора");
    }
}
