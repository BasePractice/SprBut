/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Превращает исключения предметной области в ответы HTTP.
 *
 * <p>{@code @RestControllerAdvice} — тот же {@code @Component}, только найденный
 * не по типу, а по назначению: Spring MVC сам находит такие бины и подключает
 * их ко всем контроллерам сразу.</p>
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
     * Отсутствующий элемент.
     * @param failure Ошибка
     * @return Отсутствующий элемент
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> missing(final IllegalArgumentException failure) {
        return Map.of("error", failure.getMessage());
    }

    /**
     * Значение {@code conflict}.
     * @param failure Ошибка
     * @return Значение {@code conflict}
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(final IllegalStateException failure) {
        return Map.of("error", failure.getMessage());
    }

    /**
     * Значение {@code invalid}.
     * @param failure Ошибка
     * @return Значение {@code invalid}
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(final MethodArgumentNotValidException failure) {
        return Map.of(
            "error",
            failure.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(field -> String.format("%s: %s", field.getField(), field.getDefaultMessage()))
                .orElse("некорректный запрос")
        );
    }
}
