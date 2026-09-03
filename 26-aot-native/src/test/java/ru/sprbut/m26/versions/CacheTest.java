/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m26.versions;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Тесты жизненного цикла бина с аннотациями {@code jakarta.annotation}.
 * @since 1.0
 */
@DisplayName("Слайд «Версии»: jakarta.annotation в жизненном цикле бина")
final class CacheTest {

    @Test
    @DisplayName("@PostConstruct из jakarta вызывается контейнером Boot 3")
    void callsPostConstruct() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(Cache.class);
            ctx.refresh();
            MatcherAssert.assertThat(
                "jakarta PostConstruct cannot run after the bean is built",
                ctx.getBean(Cache.class).events(),
                Matchers.contains("warm")
            );
        }
    }

    @Test
    @DisplayName("@PreDestroy срабатывает при закрытии контекста")
    void callsPreDestroy() {
        final Cache cache;
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(Cache.class);
            ctx.refresh();
            cache = ctx.getBean(Cache.class);
        }
        MatcherAssert.assertThat(
            "jakarta PreDestroy cannot run when the context closes",
            cache.events(),
            Matchers.contains("warm", "flush")
        );
    }

    @Test
    @DisplayName("вне контейнера аннотации не значат ничего — это метаданные, а не поведение")
    void dontRunLifecycleWithoutContainer() {
        MatcherAssert.assertThat(
            "annotations alone cannot trigger the lifecycle",
            new Cache().events().size(),
            Matchers.equalTo(0)
        );
    }
}
