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
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Фильтр, который записывает в журнал тело запроса.
 *
 * <p>{@code ContentCachingRequestWrapper} — штатная замена самописной
 * обёртке вроде {@link SanitizedRequest}, и работает она иначе.
 * {@code SanitizedRequest} читает тело сразу и отдаёт дальше копию,
 * а кэширующая обёртка ничего не читает сама: она запоминает байты,
 * пока их читает кто-то другой.</p>
 *
 * <p>Отсюда главное ограничение: до вызова {@code chain.doFilter} кэш
 * пуст. Записать тело в журнал можно только после того, как контроллер
 * его прочитал, а если метод тело не читал, журнал увидит пустую строку.
 * Изменить тело до контроллера такая обёртка не может вовсе.</p>
 *
 * <p>Фильтр не помечен {@code @Component}: его регистрирует
 * {@code FiltersConfig}, чтобы ограничить путями {@code /api/*}.</p>
 *
 * @since 1.0
 */
public final class AuditFilter extends OncePerRequestFilter {

    /**
     * Журнал, куда уходят записи о телах запросов.
     */
    private final Consumer<String> journal;

    /**
     * Сколько байтов тела запоминать.
     */
    private final int limit;

    /**
     * Основной конструктор.
     * @param journal Журнал, куда уходят записи о телах запросов
     * @param limit Сколько байтов тела запоминать
     * @checkstyle ConstructorsCodeFreeCheck (8 lines)
     */
    public AuditFilter(final @NonNull Consumer<String> journal, final int limit) {
        super();
        this.journal = Objects.requireNonNull(journal, "журнал тел запросов не передан");
        this.limit = limit;
    }

    @Override
    protected void doFilterInternal(
            final @NonNull HttpServletRequest request, final @NonNull HttpServletResponse response,
            final FilterChain chain
    ) throws ServletException, IOException {
        final ContentCachingRequestWrapper cached =
            new ContentCachingRequestWrapper(request, this.limit);
        chain.doFilter(cached, response);
        this.journal.accept(
            String.format(
                "тело запроса %s %s: %s",
                request.getMethod(), request.getRequestURI(), cached.getContentAsString()
            )
        );
    }
}
