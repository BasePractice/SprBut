/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m21.circular;

/**
 * Книга проводок — вторая вершина циклической зависимости.
 * @since 1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface Ledger {

    /**
     * Количество проводок.
     * @return Число записей
     */
    int entries();
}
