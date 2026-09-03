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

/**
 * Тесты правила переезда с {@code javax} на {@code jakarta}.
 * @since 1.0
 */
@DisplayName("Слайд «Версии»: переезд javax в jakarta")
final class MigrationTest {

    @Test
    @DisplayName("аннотации Java EE переезжают в jakarta")
    void movesAnnotationsToJakarta() {
        MatcherAssert.assertThat(
            "javax annotation package cannot move to jakarta",
            new Migration("javax.annotation.PostConstruct").target(),
            Matchers.equalTo("jakarta.annotation.PostConstruct")
        );
    }

    @Test
    @DisplayName("сервлеты и персистентность переезжают вместе со всем Java EE")
    void movesPersistenceToJakarta() {
        MatcherAssert.assertThat(
            "javax persistence package cannot move to jakarta",
            new Migration("javax.persistence.Entity").target(),
            Matchers.equalTo("jakarta.persistence.Entity")
        );
    }

    @Test
    @DisplayName("javax.annotation.processing остаётся на месте — это JDK, а не Java EE")
    void dontMoveAnnotationProcessing() {
        MatcherAssert.assertThat(
            "annotation processing API cannot stay in javax",
            new Migration("javax.annotation.processing.Processor").target(),
            Matchers.startsWith("javax.")
        );
    }

    @Test
    @DisplayName("чужие пакеты переезд не трогает")
    void dontTouchForeignPackages() {
        MatcherAssert.assertThat(
            "unrelated package cannot survive the migration untouched",
            new Migration("org.springframework.stereotype.Service").target(),
            Matchers.equalTo("org.springframework.stereotype.Service")
        );
    }
}
