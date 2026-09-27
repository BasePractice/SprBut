/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.handlers;

import java.util.Locale;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Четвёртый участник: резолвер аргумента.
 *
 * <p>Конвертер отвечает на вопрос «во что превратить значение», резолвер —
 * на вопрос «откуда значение взять». {@code @PathVariable},
 * {@code @RequestParam} и {@code @RequestBody} — это тоже резолверы,
 * просто встроенные; свой пишется тогда, когда источник аргумента
 * не описывается ни одним из них.</p>
 *
 * <p>Резолвер сам отвечает, за какие параметры он берётся
 * ({@code supportsParameter}), и сам собирает значение
 * ({@code resolveArgument}). Аннотации {@code @Component} для этого мало:
 * список резолверов диспетчер получает не из контейнера, а от
 * {@code WebMvcConfigurer} — см. {@code HandlersConfig}.</p>
 *
 * @since 1.0
 */
public final class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public CurrentUserArgumentResolver() {
        // нечего инициализировать
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter, final ModelAndViewContainer container,
        final NativeWebRequest request, final WebDataBinderFactory factory
    ) {
        final String raw = request.getHeader("X-User");
        final String user;
        if (raw == null) {
            user = "anonymous";
        } else {
            user = raw.trim().toLowerCase(Locale.ROOT);
        }
        return user;
    }
}
