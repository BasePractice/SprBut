/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m20.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Слайд 199: {@code @ControllerAdvice} собирает обработку ошибок в одном месте.
 *
 * <p>Без него каждый контроллер ловил бы исключения сам, и формат ответа
 * расходился бы от метода к методу. Совет применяется ко всем контроллерам
 * сразу, поэтому ответ на любую ошибку выглядит одинаково.</p>
 *
 * <p>{@code ProblemDetail} — формат из RFC 9457, штатный ответ об ошибке
 * начиная с Spring 6: тип, заголовок, код, подробности.</p>
 *
 * @since 1.0
 */
@RestControllerAdvice
public final class Failures {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Failures() {
        // нечего инициализировать
    }

    /**
     * Ошибки проверки тела запроса становятся ответом 400.
     * @param error Исключение проверки
     * @return Ответ 400 с перечнем нарушенных правил
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(final MethodArgumentNotValidException error) {
        final ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "заметка не прошла проверку"
        );
        detail.setProperty("errors", Failures.messages(error));
        return detail;
    }

    private static List<String> messages(final MethodArgumentNotValidException error) {
        return error.getBindingResult().getFieldErrors().stream()
            .map(field -> String.format("%s: %s", field.getField(), field.getDefaultMessage()))
            .sorted()
            .toList();
    }
}
