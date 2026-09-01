/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

import java.util.List;

/**
 * Итог проверки объекта.
 *
 * <p>Возвращается целиком, а не в виде «валиден да/нет»: пользователю формы нужно
 * знать все ошибки сразу, а не по одной за отправку.</p>
 *
 * @param violations все найденные нарушения
 * @since 1.0
 */
public record Verdict(List<Violation> violations) {

    /**
     * Значение {@code Verdict}.
     */
    public Verdict {
        violations = List.copyOf(violations);
    }

    /**
     * Нет ли нарушений вовсе.
     */
    public boolean valid() {
        return this.violations.isEmpty();
    }

    /**
     * Сообщения обо всех нарушениях.
     */
    public List<String> messages() {
        return this.violations.stream().map(Violation::toString).toList();
    }

    /**
     * Имена полей, не прошедших проверку, без повторов.
     */
    public List<String> fields() {
        return this.violations.stream().map(Violation::field).distinct().sorted().toList();
    }
}
