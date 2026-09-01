/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

/**
 * Одно нарушение: какое поле, что не так и какое было значение.
 *
 * <p>Отвергнутое значение хранится вместе с сообщением намеренно: сообщение
 * без него объясняет правило, но не объясняет, почему оно не выполнено.</p>
 *
 * @param field    имя поля
 * @param message  что именно нарушено
 * @param rejected значение, которое не прошло проверку
 * @since 1.0
 */
public record Violation(String field, String message, Object rejected) {

    @Override
    public String toString() {
        return String.format("%s: %s (было: %s)", this.field, this.message, this.rejected);
    }
}
