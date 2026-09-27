/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Второй участник: интерсептор диспетчера.
 *
 * <p>Интерсептор живёт внутри {@code DispatcherServlet} и в этом его
 * отличие от фильтра: обработчик к моменту вызова {@code preHandle} уже
 * выбран и передан третьим аргументом. Зато сырого потока запроса
 * интерсептору уже не достать — тело к этому времени прочитано.</p>
 *
 * <p>{@code preHandle} умеет прервать обработку, вернув {@code false}:
 * так работают проверки доступа, написанные без Spring Security.
 * Здесь же он только запоминает отметку времени, а {@code postHandle}
 * превращает разницу в заголовок ответа — между двумя вызовами лежит
 * ровно работа метода контроллера.</p>
 *
 * @since 1.0
 */
public final class TimingInterceptor implements HandlerInterceptor {

    /**
     * Атрибут запроса с отметкой начала обработки.
     */
    private static final String STARTED = "sprbut.started";

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public TimingInterceptor() {
        // нечего инициализировать
    }

    @Override
    public boolean preHandle(
        final HttpServletRequest request, final HttpServletResponse response, final Object handler
    ) {
        request.setAttribute(TimingInterceptor.STARTED, System.nanoTime());
        return true;
    }

    @Override
    public void postHandle(
        final HttpServletRequest request, final HttpServletResponse response,
        final Object handler, final ModelAndView model
    ) {
        response.setHeader(
            "X-Elapsed-Nanos",
            String.valueOf(
                System.nanoTime() - (long) request.getAttribute(TimingInterceptor.STARTED)
            )
        );
    }
}
