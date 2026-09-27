/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ProtectedMethodInFinalClassCheck disable
package ru.sprbut.m28.web;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
 * <p>Базовый класс {@code ResponseEntityExceptionHandler} уже умеет
 * превращать в {@code ProblemDetail} полтора десятка исключений самого
 * Spring MVC: неразборчивый JSON, неподдержанный метод HTTP, не тот тип
 * параметра, {@code ResponseStatusException}. Поэтому свой ответ на
 * проверку тела здесь не объявлен через {@code @ExceptionHandler}, а
 * переопределён: базовый класс уже держит обработчик для
 * {@code MethodArgumentNotValidException}, и второй такой же контекст
 * отверг бы на старте как неоднозначный.</p>
 *
 * @since 1.0
 */
@RestControllerAdvice
public final class Failures extends ResponseEntityExceptionHandler {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Failures() {
        super();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        final MethodArgumentNotValidException error, final HttpHeaders headers,
        final HttpStatusCode status, final WebRequest request
    ) {
        final ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            status, "запрос не прошёл проверку"
        );
        detail.setTitle("Запрос отвергнут");
        detail.setProperty("errors", Failures.messages(error));
        return this.handleExceptionInternal(error, detail, headers, status, request);
    }

    private static List<String> messages(final MethodArgumentNotValidException error) {
        return error.getBindingResult().getFieldErrors().stream()
            .map(field -> String.format("%s: %s", field.getField(), field.getDefaultMessage()))
            .sorted()
            .toList();
    }
}
