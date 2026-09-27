/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.returns;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Обработчик возвращаемого значения: зеркало резолвера аргумента.
 *
 * <p>Резолвер решает, откуда взять аргумент метода; этот обработчик —
 * куда деть результат. Он пишет ответ сам и сообщает диспетчеру, что
 * запрос обработан ({@code setRequestHandled}): иначе диспетчер стал бы
 * искать представление с именем, которого нет.</p>
 *
 * <p>Главная ловушка — порядок. Свои обработчики встают после штатных,
 * а штатный {@code RequestResponseBodyMethodProcessor} берётся за любой
 * тип, если метод или класс помечены {@code @ResponseBody}. В
 * {@code @RestController} помечено всё, и до этого обработчика очередь
 * не дойдёт никогда: {@code Lines} уйдёт в Jackson и станет
 * {@code {"items": [...]}}. Работает он только в обычном
 * {@code @Controller} — см. {@code PlainController}.</p>
 *
 * @since 1.0
 */
public final class LinesReturnValueHandler implements HandlerMethodReturnValueHandler {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public LinesReturnValueHandler() {
        // нечего инициализировать
    }

    @Override
    public boolean supportsReturnType(final MethodParameter type) {
        return Lines.class.equals(type.getParameterType());
    }

    @Override
    public void handleReturnValue(
        final Object value, final @NonNull MethodParameter type,
        final ModelAndViewContainer container, final NativeWebRequest request
    ) throws IOException {
        container.setRequestHandled(true);
        final HttpServletResponse response = Objects.requireNonNull(
            request.getNativeResponse(HttpServletResponse.class)
        );
        response.setContentType("text/plain;charset=UTF-8");
        final PrintWriter writer = response.getWriter();
        for (final String item : ((Lines) Objects.requireNonNull(value)).items()) {
            writer.append(item).append('\n');
        }
    }
}
