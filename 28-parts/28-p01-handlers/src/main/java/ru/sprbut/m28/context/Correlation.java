/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.context;

import org.jspecify.annotations.NonNull;

/**
 * Идентификатор запроса, доступный любому бину.
 *
 * <p>Интерфейс здесь не ради абстракции: бин области запроса внедряется
 * в одиночки через прокси, а для {@code final} класса, каким является
 * запись, прокси можно построить только по интерфейсу.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Correlation {

    /**
     * Идентификатор текущего запроса.
     * @return Идентификатор
     */
    @NonNull String id();
}
