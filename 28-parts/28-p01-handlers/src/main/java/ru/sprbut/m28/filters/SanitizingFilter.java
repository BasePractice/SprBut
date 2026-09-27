/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Первый участник: фильтр сервлет-контейнера.
 *
 * <p>Фильтр стоит до {@code DispatcherServlet} и потому не знает ни о
 * контроллерах, ни о маршрутах — он видит сырой запрос и сырой ответ.
 * Всё, что можно сделать на этом уровне, делается с байтами: здесь из тела
 * вырезаются теги {@code <script>}.</p>
 *
 * <p>Тело запроса читается ровно один раз, поэтому фильтр отдаёт дальше
 * не исходный запрос, а обёртку {@link SanitizedRequest} с уже вычищенным
 * телом. Без обёртки контроллер получил бы пустой поток.</p>
 *
 * <p>Фильтр-бин контейнер регистрирует сам — в отличие от интерсептора
 * и резолвера аргумента, которым нужен {@code WebMvcConfigurer}.</p>
 *
 * @since 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class SanitizingFilter implements Filter {

    /**
     * Теги, которых в теле запроса быть не должно.
     */
    private static final Pattern SCRIPT = Pattern.compile(
        "(?i)<script.*?>.*?</script>", Pattern.DOTALL
    );

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SanitizingFilter() {
        // нечего инициализировать
    }

    @Override
    public void doFilter(
        final ServletRequest req, final ServletResponse res, final FilterChain chain
    ) throws IOException, ServletException {
        final ServletRequest passed;
        if (SanitizingFilter.json(req)) {
            final HttpServletRequest request = (HttpServletRequest) req;
            passed = new SanitizedRequest(request, SanitizingFilter.clean(request));
        } else {
            passed = req;
        }
        chain.doFilter(passed, res);
    }

    // тело вычищается только у JSON: у формы его читает сам контейнер,
    // и перехваченный поток сломал бы getParameter
    private static boolean json(final ServletRequest req) {
        final String type = req.getContentType();
        return type != null && type.startsWith(MediaType.APPLICATION_JSON_VALUE);
    }

    private static byte[] clean(final HttpServletRequest request) throws IOException {
        return SanitizingFilter.SCRIPT
            .matcher(new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8))
            .replaceAll("")
            .getBytes(StandardCharsets.UTF_8);
    }
}
