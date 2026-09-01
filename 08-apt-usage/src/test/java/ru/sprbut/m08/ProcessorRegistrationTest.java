/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m08;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 66: регистрация процессора через META-INF/services.
 * @since 1.0
 */
@DisplayName("Слайд 66: регистрация процессора через META-INF/services")
final class ProcessorRegistrationTest {

    @Test
    @DisplayName("файл регистрации перечисляет процессоры модуля 07")
    void listsDeclaredProcessors() {
        MatcherAssert.assertThat(
            "service file cannot list the declared processor",
            new ProcessorRegistration().declared(),
            Matchers.hasItem("ru.sprbut.m07.BuilderProcessor")
        );
    }

    @Test
    @DisplayName("ServiceLoader загружает ровно то, что объявлено в файле")
    void loadsExactlyWhatIsDeclared() {
        MatcherAssert.assertThat(
            "service loader cannot load exactly the declared processors",
            new ProcessorRegistration().loaded(),
            Matchers.equalTo(new ProcessorRegistration().declared())
        );
    }

    @Test
    @DisplayName("процессор сам объявляет, какие аннотации обрабатывает")
    void readsSupportedAnnotations() {
        MatcherAssert.assertThat(
            "processor cannot declare its supported annotations",
            new ProcessorRegistration().supported("ru.sprbut.m07.BuilderProcessor"),
            Matchers.hasItem(Matchers.containsString("Builder"))
        );
    }

    @Test
    @DisplayName("незарегистрированный процессор — понятная ошибка с его именем")
    void failsOnUnregisteredProcessor() {
        MatcherAssert.assertThat(
            "unregistered processor cannot be reported by name",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProcessorRegistration().supported("ru.sprbut.Nope")
            ).getMessage(),
            Matchers.containsString("ru.sprbut.Nope")
        );
    }

    @Test
    @DisplayName("путь регистрации — часть контракта javac, а не выдумка проекта")
    void keepsStandardServicePath() {
        MatcherAssert.assertThat(
            "service path cannot follow the javac contract",
            new ProcessorRegistration().servicePath(),
            Matchers.equalTo("META-INF/services/javax.annotation.processing.Processor")
        );
    }
}
