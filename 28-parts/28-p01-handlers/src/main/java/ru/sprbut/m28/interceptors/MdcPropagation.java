/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.interceptors;

import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;

/**
 * Интерсептор асинхронной обработки: переносит MDC в рабочий поток.
 *
 * <p>Метод, вернувший {@code Callable}, освобождает поток контейнера,
 * а сам {@code Callable} выполняется в другом потоке. Всё, что лежало
 * в {@code ThreadLocal} потока запроса, там и остаётся. Свои
 * {@code ThreadLocal} Spring переносит сам: {@code FrameworkServlet}
 * регистрирует такой же интерсептор для {@code RequestContextHolder} и
 * {@code LocaleContextHolder}, поэтому бины области запроса в
 * {@code Callable} работают. О чужих он не знает, и MDC журнала —
 * первый из них: строки лога из рабочего потока теряют идентификатор
 * запроса.</p>
 *
 * <p>Интерсептор вызывается на обеих сторонах границы.
 * {@code beforeConcurrentHandling} — ещё в потоке запроса: здесь MDC
 * снимается в атрибут. {@code preProcess} и {@code postProcess} — в
 * рабочем потоке, до {@code Callable} и после, даже если тот упал:
 * здесь MDC восстанавливается и очищается, чтобы поток из пула не унёс
 * его к следующей задаче.</p>
 *
 * <p>Обычный {@code HandlerInterceptor} тут не поможет: он работает
 * только в потоке контейнера.</p>
 *
 * @since 1.0
 */
public final class MdcPropagation implements CallableProcessingInterceptor {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public MdcPropagation() {
        // нечего инициализировать
    }

    @Override
    public <T> void beforeConcurrentHandling(
        final NativeWebRequest request, final Callable<T> task
    ) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        if (context != null) {
            request.setAttribute("sprbut.mdc", context, RequestAttributes.SCOPE_REQUEST);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void preProcess(final NativeWebRequest request, final Callable<T> task) {
        if (request.getAttribute("sprbut.mdc", RequestAttributes.SCOPE_REQUEST)
            instanceof Map<?, ?> context) {
            MDC.setContextMap((Map<String, String>) context);
        }
    }

    @Override
    public <T> void postProcess(
        final NativeWebRequest request, final Callable<T> task, final Object result
    ) {
        MDC.clear();
    }
}
