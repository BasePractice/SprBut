/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle ProtectedMethodInFinalClassCheck disable
package ru.sprbut.m28.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр, который гарантированно срабатывает один раз на запрос.
 *
 * <p>Запрос может пройти через контейнер сервлетов не единожды: после
 * исключения контейнер заново диспетчеризует его на страницу ошибки,
 * {@code forward} отправляет его по второму кругу. Голый {@code Filter},
 * зарегистрированный на такие проходы, отработал бы на каждом, и ответ
 * об ошибке получил бы новый идентификатор вместо того, с которым
 * пришёл запрос.</p>
 *
 * <p>{@code OncePerRequestFilter} помечает запрос атрибутом и на
 * повторных проходах в {@code doFilterInternal} не заходит. Boot учитывает
 * это: такой фильтр он регистрирует на все типы диспетчеризации, а
 * обычный — только на {@code REQUEST}. Бонусом базовый класс сам
 * приводит запрос к {@code HttpServletRequest}.</p>
 *
 * <p>Идентификатор кладётся в атрибут запроса: дальше его читают
 * контроллер ({@code @RequestAttribute}) и бин области запроса
 * ({@code CorrelationConfig}). Ещё одна копия уходит в MDC журнала,
 * чтобы каждая строка лога этого запроса несла идентификатор; снять её
 * нужно в {@code finally}, иначе поток контейнера унесёт её к чужому
 * запросу.</p>
 *
 * @since 1.0
 */
@Component
public final class CorrelationFilter extends OncePerRequestFilter {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public CorrelationFilter() {
        super();
    }

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request, final HttpServletResponse response,
        final FilterChain chain
    ) throws ServletException, IOException {
        final String id = CorrelationFilter.id(request);
        request.setAttribute("sprbut.correlation", id);
        response.setHeader("X-Correlation-Id", id);
        MDC.put("correlation", id);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("correlation");
        }
    }

    private static String id(final HttpServletRequest request) {
        final String header = request.getHeader("X-Correlation-Id");
        final String id;
        if (header == null || header.isBlank()) {
            id = UUID.randomUUID().toString();
        } else {
            id = header.trim();
        }
        return id;
    }
}
