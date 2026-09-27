/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m28.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Шестой участник: совет над ошибкой.
 *
 * <p>{@code MaskingRequestBodyAdvice} вмешивается в удачный путь запроса,
 * этот — в неудачный. Проверка {@code @Valid} заканчивается исключением,
 * и без совета клиент получил бы код 400 с пустым телом: что именно
 * не понравилось серверу, осталось бы в логах.</p>
 *
 * <p>{@code ProblemDetail} — формат из RFC 9457, штатный ответ об ошибке
 * начиная с Spring 6. Перечень нарушенных правил кладётся в него
 * отдельным свойством {@code errors}, по пункту на поле: клиенту нужно
 * знать не только что запрос отвергнут, но и какое поле чинить.</p>
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
     * Нарушенные правила проверки становятся ответом 400.
     * @param error Исключение проверки
     * @return Ответ 400 с перечнем нарушенных правил
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(final MethodArgumentNotValidException error) {
        final ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "пользователь не прошёл проверку"
        );
        detail.setTitle("Тело запроса отвергнуто");
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
