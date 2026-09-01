/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Audited;

/**
 * Интерфейс с аннотацией. {@code @Inherited} на интерфейсы не распространяется
 * вовсе — это третья и самая неожиданная его граница.
 * @since 1.0
 */
@Audited(actor = "интерфейс")
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface AuditedContract {

    /**
     * Действие контракта.
     * @return Действие контракта
     */
    String action();
}
