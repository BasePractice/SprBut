/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.config;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import ru.sprbut.m28.errors.UnsupportedResolver;
import ru.sprbut.m28.handlers.CurrentUserArgumentResolver;
import ru.sprbut.m28.returns.LinesReturnValueHandler;

/**
 * Регистрация участников, которых контейнер не найдёт сам.
 * @since 1.0
 */
final class HandlersConfigTest {

    @Test
    @DisplayName("конфигурация добавляет резолвер в список диспетчера")
    void registersResolver() {
        final List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(1);
        new HandlersConfig().addArgumentResolvers(resolvers);
        MatcherAssert.assertThat(
            "резолвер не попал в список диспетчера",
            resolvers,
            Matchers.hasItem(Matchers.instanceOf(CurrentUserArgumentResolver.class))
        );
    }

    @Test
    @DisplayName("конфигурация добавляет обработчик результата в список диспетчера")
    void registersReturnValueHandler() {
        final List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(1);
        new HandlersConfig().addReturnValueHandlers(handlers);
        MatcherAssert.assertThat(
            "обработчик результата не попал в список диспетчера",
            handlers,
            Matchers.hasItem(Matchers.instanceOf(LinesReturnValueHandler.class))
        );
    }

    @Test
    @DisplayName("свой обработчик исключений встаёт после штатных, а не вместо них")
    void appendsExceptionResolver() {
        final List<HandlerExceptionResolver> resolvers = new ArrayList<>(2);
        resolvers.add(new DefaultHandlerExceptionResolver());
        new HandlersConfig().extendHandlerExceptionResolvers(resolvers);
        MatcherAssert.assertThat(
            "штатные обработчики исключений потерялись или стоят не первыми",
            resolvers,
            Matchers.contains(
                Matchers.instanceOf(DefaultHandlerExceptionResolver.class),
                Matchers.instanceOf(UnsupportedResolver.class)
            )
        );
    }
}
