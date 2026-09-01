/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m14.extended;

/**
 * Нарушение ожидаемого порядка шагов.
 * @param rule   правило, которое нарушено
 * @param detail что именно пошло не так
 * @since 1.0
 */
public record Violation(String rule, String detail) {
}
