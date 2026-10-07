/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m29.identity;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Аутентификация по заголовкам X-User-*, выставленным gateway.
 *
 * <p>Токен здесь не проверяется: сервис доверяет сети, наружу открыт
 * только gateway, а он затирает такие заголовки, пришедшие от клиента.</p>
 *
 * @since 1.0
 */
public final class HeaderAuthentication implements Filter {

    /**
     * Конструктор.
     */
    public HeaderAuthentication() {
        // Состояния у фильтра нет.
    }

    @Override
    public void doFilter(
        final ServletRequest request,
        final ServletResponse response,
        final FilterChain chain
    ) throws IOException, ServletException {
        new Arrived(((HttpServletRequest) request)::getHeader).identity().ifPresent(
            identity -> SecurityContextHolder.getContext().setAuthentication(
                identity.authentication()
            )
        );
        chain.doFilter(request, response);
    }
}
