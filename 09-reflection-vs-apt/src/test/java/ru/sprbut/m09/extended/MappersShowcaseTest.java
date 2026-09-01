/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m09.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m09.model.UserEntity;

/**
 * Расширенный пример: СХЕМА 4 — compile-time против runtime.
 * @since 1.0
 */
@DisplayName("Расширенный пример: СХЕМА 4 — compile-time против runtime")
final class MappersShowcaseTest {

    private static UserEntity entity() {
        return new UserEntity("U-1", "Иван", "Иванов", 42, true);
    }

    @Test
    @DisplayName("механизм не меняет поведение — только свойства")
    void agreeOnResult() {
        MatcherAssert.assertThat(
            "three mechanisms cannot agree on the result",
            new Mappers().agree(entity()),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("реализаций ровно три — по одной на механизм со слайда")
    void coversThreeMechanisms() {
        MatcherAssert.assertThat(
            "showcase cannot cover all three mechanisms",
            new Mappers().list(),
            Matchers.hasSize(3)
        );
    }

    @Test
    @DisplayName("каждая реализация объявляет свою стратегию")
    void namesEveryStrategy() {
        MatcherAssert.assertThat(
            "every mapper cannot name its own strategy",
            new Mappers().strategies(),
            Matchers.hasSize(3)
        );
    }

    @Test
    @DisplayName("замер даёт число для каждой реализации")
    void measuresEveryMapper() {
        MatcherAssert.assertThat(
            "benchmark cannot measure the reflective mapper",
            new Benchmark(new Mappers(), entity()).timings(100),
            Matchers.hasKey("ReflectiveMapper")
        );
    }

    @Test
    @DisplayName("рефлексии для native image нужны подсказки на каждое свойство")
    void demandsHintsForReflection() {
        MatcherAssert.assertThat(
            "reflective mapper cannot demand runtime hints",
            new RequiredHints().accessors(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    @Test
    @DisplayName("подсказка называет конкретный метод, а не класс целиком")
    void namesExactAccessor() {
        MatcherAssert.assertThat(
            "hint cannot name the exact accessor",
            new RequiredHints().accessors(),
            Matchers.hasItem("UserEntity#getFirstName")
        );
    }

    @Test
    @DisplayName("сгенерированному коду подсказки не нужны вовсе")
    void demandsNoHintsForGeneratedCode() {
        MatcherAssert.assertThat(
            "generated mapper cannot avoid runtime hints entirely",
            new RequiredHints().byMapper().get("GeneratedStyleMapper"),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("для байткода вопрос подсказок не имеет смысла — класса ещё нет")
    void reportsBytecodeAsInapplicable() {
        MatcherAssert.assertThat(
            "bytecode mapper cannot be reported as inapplicable for native image",
            new RequiredHints().byMapper().get("BytecodeMapper"),
            Matchers.hasSize(1)
        );
    }
}
