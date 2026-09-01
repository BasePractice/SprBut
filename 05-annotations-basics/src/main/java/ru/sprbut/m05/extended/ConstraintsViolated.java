/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

/**
 * Отказ режима «падать сразу».
 *
 * <p>Несёт весь вердикт целиком, а не только первое нарушение: сообщение об одной
 * ошибке из пяти заставляет чинить их по очереди, по одной за запуск.</p>
 *
 * @since 1.0
 */
public final class ConstraintsViolated extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Вердикт.
     */
    private final transient Verdict verdict;

    /**
     * Основной конструктор.
     * @param verdict Вердикт
     */
    public ConstraintsViolated(final Verdict verdict) {
        super(
            "Нарушений: " + verdict.violations().size()
                + " — " + String.join("; ", verdict.messages())
        );
        this.verdict = verdict;
    }

    /**
     * Полный вердикт со всеми нарушениями.
     * @return Полный вердикт со всеми нарушениями
     */
    public Verdict verdict() {
        return this.verdict;
    }
}
