/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m26.hints;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ImportRuntimeHints;
import ru.sprbut.m26.reflection.Plugin;

/**
 * Тесты конфигурации, объявляющей свою рефлексию.
 * @since 1.0
 */
@DisplayName("Слайд «AOT»: конфигурация объявляет свою рефлексию через @ImportRuntimeHints")
final class PluginConfigTest {

    @Test
    @DisplayName("бин собирается рефлексией и попадает в контекст")
    void buildsPluginReflectively() {
        final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(PluginConfig.class);
        try (context) {
            MatcherAssert.assertThat(
                "reflectively built plugin cannot reach the context",
                context.getBean(Plugin.class).name(),
                Matchers.equalTo("csv")
            );
        }
    }

    @Test
    @DisplayName("регистратор подсказок объявлен на самой конфигурации")
    void declaresHintsRegistrar() {
        MatcherAssert.assertThat(
            "configuration cannot point at its own hints registrar",
            PluginConfig.class.getAnnotation(ImportRuntimeHints.class).value(),
            Matchers.arrayContaining(Matchers.equalTo(PluginHints.class))
        );
    }
}
