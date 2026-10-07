/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m29.profile;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Перевод доменных отказов в HTTP-ответы.
 * @since 1.0
 */
@RestControllerAdvice
public final class Failures {

    /**
     * Конструктор для Spring.
     */
    private Failures() {
    }

    /**
     * Логин уже занят.
     * @param error Нарушение уникальности
     * @return Ответ 409
     */
    @ExceptionHandler(DuplicateKeyException.class)
    static ProblemDetail duplicate(final DuplicateKeyException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Логин уже занят");
    }
}
