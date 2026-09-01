/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Расширенный пример: валидация на собственных аннотациях.
 * @since 1.0
 */
@DisplayName("Расширенный пример: валидация на собственных аннотациях")
final class ValidatedTest {

    @Test
    @DisplayName("корректный объект нарушений не даёт")
    void acceptsValidObject() {
        MatcherAssert.assertThat(
            "valid object cannot pass without violations",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru")).verdict().valid(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("маркерная @NotBlank срабатывает на пробелах")
    void rejectsBlankValue() {
        MatcherAssert.assertThat(
            "marker annotation cannot reject a blank value",
            new Validated(new User("U-1", "   ", 33, "ivan@mail.ru")).verdict().fields(),
            Matchers.contains("login")
        );
    }

    @Test
    @DisplayName("@MaxLength(10) читается без имени элемента")
    void appliesSingleValueAnnotation() {
        MatcherAssert.assertThat(
            "single value annotation cannot limit the length",
            new Validated(new User("U-1", "оченьдлинныйлогин", 33, "ivan@mail.ru"))
                .verdict().messages().toString(),
            Matchers.containsString("превышает максимум 10")
        );
    }

    @Test
    @DisplayName("@Range использует заданные значения элементов")
    void appliesExplicitRange() {
        MatcherAssert.assertThat(
            "explicit range values cannot be applied",
            new Validated(new User("U-1", "ivan", 5, "ivan@mail.ru"))
                .verdict().messages().toString(),
            Matchers.containsString("возраст вне диапазона")
        );
    }

    @Test
    @DisplayName("повторяемая @Matches проверяет все шаблоны, а не первый")
    void checksEveryRepeatedPattern() {
        MatcherAssert.assertThat(
            "repeatable annotation cannot check every pattern",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail")).verdict().messages(),
            Matchers.hasItem(Matchers.containsString("нет доменной зоны"))
        );
    }

    @Test
    @DisplayName("ограничения родительского класса действуют на наследника")
    void collectsConstraintsUpTheHierarchy() {
        MatcherAssert.assertThat(
            "parent constraints cannot reach the child",
            new Validated(new User(null, "ivan", 33, "ivan@mail.ru")).verdict().fields(),
            Matchers.contains("id")
        );
    }

    @Test
    @DisplayName("поле без ограничений не проверяется вовсе")
    void dontCheckUnconstrainedField() {
        MatcherAssert.assertThat(
            "unconstrained field cannot stay out of the report",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru", null, null))
                .verdict().fields(),
            Matchers.not(Matchers.hasItem("free"))
        );
    }

    @Test
    @DisplayName("ограничение с retention CLASS движок не видит — оно молча не работает")
    void dontSeeClassRetainedConstraint() {
        MatcherAssert.assertThat(
            "class retained constraint cannot stay invisible",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru", null, "текст"))
                .verdict().fields(),
            Matchers.not(Matchers.hasItem("invisible"))
        );
    }

    @Test
    @DisplayName("вердикт собирает все нарушения разом, а не первое")
    void collectsAllViolations() {
        MatcherAssert.assertThat(
            "verdict cannot collect every violation at once",
            new Validated(new User(null, "", 5, "нет")).verdict().violations(),
            Matchers.hasSize(5)
        );
    }

    @Test
    @DisplayName("режим «падать сразу» приносит с собой полный отчёт")
    void failsFastWithFullReport() {
        MatcherAssert.assertThat(
            "fail fast mode cannot carry the whole report",
            Assertions.assertThrows(
                ConstraintsViolated.class,
                () -> new Validated(new User(null, "", 5, "нет")).check()
            ).verdict().violations(),
            Matchers.hasSize(5)
        );
    }

    @Test
    @DisplayName("корректный объект режим «падать сразу» пропускает молча")
    void passesValidObjectSilently() {
        new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru")).check();
        MatcherAssert.assertThat(
            "valid object cannot pass the fail fast check silently",
            new Validated(new User("U-1", "ivan", 33, "ivan@mail.ru")).verdict().messages(),
            Matchers.hasSize(0)
        );
    }
}
