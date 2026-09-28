/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

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
 * Здесь же он только ставит отметку времени.</p>
 *
 * <p>Заголовок с длительностью пишет не {@code postHandle}, хотя кажется,
 * что место для него именно там. Для {@code @ResponseBody} конвертер
 * сообщений записывает и сбрасывает тело ещё внутри вызова обработчика,
 * ответ к {@code postHandle} уже отправлен, и настоящий контейнер
 * сервлетов молча выбрасывает заголовки, выставленные после этого.
 * {@code MockMvc} такой ошибки не покажет: его ответ принимает заголовки
 * в любой момент. Заголовок пишет {@code ElapsedResponseAdvice} —
 * последний, кто трогает ответ до записи тела.</p>
 *
 * <p>Отметка ставится только один раз. У асинхронного метода
 * {@code preHandle} вызывается дважды: при исходной диспетчеризации и
 * при повторной, когда результат готов. Перезаписанная отметка измерила
 * бы только вторую половину.</p>
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
            final HttpServletRequest request, final @NonNull HttpServletResponse response, final @NonNull Object handler
    ) {
        if (request.getAttribute(TimingInterceptor.STARTED) == null) {
            request.setAttribute(TimingInterceptor.STARTED, System.nanoTime());
        }
        return true;
    }
}
