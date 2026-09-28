/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.advices;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Совет над телом ответа: зеркало {@link MaskingRequestBodyAdvice}.
 *
 * <p>Метод уже вернул результат, конвертер сообщений уже выбран, но ещё
 * ничего не записал. Это последняя точка, где ответу можно добавить
 * заголовок: после {@code beforeBodyWrite} тело уйдёт в сеть, а вместе
 * с ним и заголовки. {@code postHandle} интерсептора для
 * {@code @ResponseBody} опаздывает — см. {@code TimingInterceptor}.</p>
 *
 * <p>Совет получает и тело, и может вернуть вместо него другое: так
 * делают конверты вида {@code {"data": ...}}. Подменять тело надо
 * осторожно: конвертер выбран по исходному типу, и если метод вернул
 * {@code String}, строковый конвертер получит конверт вместо строки и
 * упадёт на приведении типа. Этот совет тело не трогает.</p>
 *
 * <p>Советы над ответом работают только там, где работают конвертеры
 * сообщений: для {@code @ResponseBody} и {@code ResponseEntity}.
 * Ответ, записанный своим {@code HandlerMethodReturnValueHandler},
 * этот совет не увидит.</p>
 *
 * @since 1.0
 */
@ControllerAdvice
public final class ElapsedResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ElapsedResponseAdvice() {
        // нечего инициализировать
    }

    @Override
    public boolean supports(
            final @NonNull MethodParameter type, final @NonNull Class<? extends HttpMessageConverter<?>> converter
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            final Object body, final @NonNull MethodParameter type, final @NonNull MediaType media,
            final @NonNull Class<? extends HttpMessageConverter<?>> converter,
            final @NonNull ServerHttpRequest request, final @NonNull ServerHttpResponse response
    ) {
        final Object started = ((ServletServerHttpRequest) request).getServletRequest()
            .getAttribute("sprbut.started");
        if (started instanceof Long nanos) {
            response.getHeaders().set(
                "X-Elapsed-Nanos", String.valueOf(System.nanoTime() - nanos)
            );
        }
        return body;
    }
}
