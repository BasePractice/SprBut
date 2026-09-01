/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01.extended;

/**
 * Строка, безопасная для вставки в JSON.
 *
 * <p>Кавычки и обратные слэши экранируются, переводы строк и табуляции
 * заменяются escape-последовательностями. Без этого любой текст
 * с кавычкой ломал бы весь документ.</p>
 *
 * @since 1.0
 */
public final class Escaped {

    /**
     * Исходное значение.
     */
    private final String raw;

    /**
     * Основной конструктор.
     * @param raw Исходное значение
     */
    public Escaped(final String raw) {
        this.raw = raw;
    }

    /**
     * Экранированный текст.
     * @return Экранированный текст
     */
    public String text() {
        return this.raw
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
