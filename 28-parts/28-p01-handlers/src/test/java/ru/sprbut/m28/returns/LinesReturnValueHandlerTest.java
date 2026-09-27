/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.returns;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.sprbut.m28.web.PlainController;

/**
 * Обработчик результата {@code Lines}.
 * @since 1.0
 */
final class LinesReturnValueHandlerTest {

    @Test
    @DisplayName("обработчик берётся за метод, возвращающий строки")
    void supportsLines() throws Exception {
        MatcherAssert.assertThat(
            "обработчик не узнал свой тип результата",
            new LinesReturnValueHandler().supportsReturnType(
                new MethodParameter(PlainController.class.getMethod("lines"), -1)
            ),
            Matchers.is(true)
        );
    }

    @Test
    @DisplayName("строки уходят в ответ через перевод строки")
    void writesLines() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        new LinesReturnValueHandler().handleReturnValue(
            new Lines(List.of("oleg", "nina")),
            new MethodParameter(PlainController.class.getMethod("lines"), -1),
            new ModelAndViewContainer(),
            new ServletWebRequest(new MockHttpServletRequest(), response)
        );
        MatcherAssert.assertThat(
            "строки не записаны в ответ",
            response.getContentAsString().lines().toList(),
            Matchers.contains("oleg", "nina")
        );
    }

    @Test
    @DisplayName("диспетчер узнаёт, что представление искать не нужно")
    void marksRequestHandled() throws Exception {
        final ModelAndViewContainer container = new ModelAndViewContainer();
        new LinesReturnValueHandler().handleReturnValue(
            new Lines(List.of("oleg")),
            new MethodParameter(PlainController.class.getMethod("lines"), -1),
            container,
            new ServletWebRequest(new MockHttpServletRequest(), new MockHttpServletResponse())
        );
        MatcherAssert.assertThat(
            "запрос не помечен обработанным",
            container.isRequestHandled(),
            Matchers.is(true)
        );
    }
}
