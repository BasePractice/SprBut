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
import ru.sprbut.m28.handlers.CurrentUserArgumentResolver;

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
}
