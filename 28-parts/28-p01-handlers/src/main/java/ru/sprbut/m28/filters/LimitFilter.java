/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ProtectedMethodInFinalClassCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m28.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Фильтр, который отвергает слишком длинные тела.
 *
 * <p>Исключение, брошенное из фильтра, до {@code @ExceptionHandler} не
 * дойдёт: советы над ошибками работают внутри {@code DispatcherServlet},
 * а фильтр стоит снаружи. Такое исключение поймает контейнер сервлетов,
 * и клиент получит код 500 от страницы ошибок Boot вместо
 * {@code ProblemDetail}, который отдают все остальные отказы.</p>
 *
 * <p>Поэтому фильтр ничего не бросает, а передаёт отказ тому же
 * {@code HandlerExceptionResolver}, которым пользуется диспетчер. Ответ
 * собирает {@code Failures}: {@code ResponseStatusException} его базовый
 * класс превращает в {@code ProblemDetail} с кодом 413 без единой
 * строки кода.</p>
 *
 * <p>Длина берётся из заголовка {@code Content-Length}: тело при этом не
 * читается, и следующим фильтрам оно достанется целым. Запрос, переданный
 * частями, заголовка не несёт, и такой фильтр его пропустит.</p>
 *
 * @since 1.0
 */
public final class LimitFilter extends OncePerRequestFilter {

    /**
     * Обработчик исключений диспетчера.
     */
    private final HandlerExceptionResolver resolver;

    /**
     * Наибольшая допустимая длина тела в байтах.
     */
    private final long limit;

    /**
     * Основной конструктор.
     * @param resolver Обработчик исключений диспетчера
     * @param limit Наибольшая допустимая длина тела в байтах
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public LimitFilter(final @NonNull HandlerExceptionResolver resolver, final long limit) {
        super();
        this.resolver = Objects.requireNonNull(
            resolver, "обработчик исключений диспетчера не передан"
        );
        this.limit = limit;
    }

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request, final @NonNull HttpServletResponse response,
        final @NonNull FilterChain chain
    ) throws ServletException, IOException {
        if (request.getContentLengthLong() > this.limit) {
            this.resolver.resolveException(
                request, response, null,
                new ResponseStatusException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    String.format("тело запроса длиннее %d байт", this.limit)
                )
            );
        } else {
            chain.doFilter(request, response);
        }
    }
}
