/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.extended;

import ru.sprbut.m25.Diagnosis;

/**
 * Диагноз по умолчанию: тип ошибки не распознан.
 *
 * <p>Молчать в таком случае нельзя — незнакомая поломка должна называть себя
 * классом исключения, иначе диагност начнёт врать уверенным тоном.</p>
 *
 * @since 1.0
 */
public final class UnknownFailure implements Diagnosis {

    /**
     * Нераспознанная ошибка старта.
     */
    private final Throwable cause;

    /**
     * Основной конструктор.
     * @param cause Нераспознанная ошибка старта
     */
    public UnknownFailure(final Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String summary() {
        return String.format(
            "контекст не поднялся: %s", this.cause.getClass().getSimpleName()
        );
    }

    @Override
    public String remedy() {
        return "запустить приложение с --debug и прочитать отчёт об условиях";
    }
}
