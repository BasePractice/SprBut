/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.errors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Обработчик исключений нижнего уровня: неподдержанная операция в код 501.
 *
 * <p>{@code @ExceptionHandler} — не отдельный механизм, а один из
 * {@code HandlerExceptionResolver}: диспетчер держит их цепочку и
 * опрашивает по очереди, пока кто-то не вернёт не-{@code null}. Пустой
 * {@code ModelAndView} значит «обработано, писать нечего», {@code null} —
 * «не моё, спросите следующего».</p>
 *
 * <p>В цепочку его добавляет {@code HandlersConfig} через
 * {@code extendHandlerExceptionResolvers}, то есть в конец, после
 * штатных. Соседний метод {@code configureHandlerExceptionResolvers}
 * выглядит так же, но заменяет цепочку целиком: объявив его, легко
 * остаться без {@code @ExceptionHandler} и без стандартных ответов 4xx.</p>
 *
 * @since 1.0
 */
public final class UnsupportedResolver implements HandlerExceptionResolver {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public UnsupportedResolver() {
        // нечего инициализировать
    }

    @Override
    public ModelAndView resolveException(
            final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response,
            final Object handler, final @NonNull Exception error
    ) {
        final ModelAndView view;
        if (error instanceof UnsupportedOperationException) {
            response.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
            view = new ModelAndView();
        } else {
            view = null;
        }
        return view;
    }
}
