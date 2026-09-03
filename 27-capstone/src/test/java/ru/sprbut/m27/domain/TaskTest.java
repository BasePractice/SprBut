/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.domain;

import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Домен: правила переходов живут в самой задаче.
 * @since 1.0
 */
@DisplayName("Домен: правила переходов живут в самой задаче")
final class TaskTest {

    @Test
    @DisplayName("новая задача открыта")
    void opensInOpenStatus() {
        MatcherAssert.assertThat(
            "fresh task cannot start as open",
            new Task("сверстать отчёт", Instant.parse("2026-07-30T10:00:00Z")).status(),
            Matchers.equalTo(TaskStatus.OPEN)
        );
    }

    @Test
    @DisplayName("открытая задача переходит в работу")
    void movesToInProgress() {
        final Task task = new Task("собрать логи", Instant.parse("2026-07-30T10:00:00Z"));
        task.start();
        MatcherAssert.assertThat(
            "open task cannot move to in progress",
            task.status(),
            Matchers.equalTo(TaskStatus.IN_PROGRESS)
        );
    }

    @Test
    @DisplayName("закрытая задача не открывается заново")
    void dontReopenFinishedTask() {
        final Task task = new Task("выкатить релиз", Instant.parse("2026-07-30T10:00:00Z"));
        task.start();
        task.finish();
        MatcherAssert.assertThat(
            "finished task cannot refuse to go backwards",
            Assertions.assertThrows(IllegalStateException.class, task::start).getMessage(),
            Matchers.containsString("DONE")
        );
    }

    @Test
    @DisplayName("отказ перехода не меняет состояние задачи")
    void keepsStatusOnRejectedMove() {
        final Task task = new Task("починить сборку", Instant.parse("2026-07-30T10:00:00Z"));
        task.finish();
        Assertions.assertThrows(IllegalStateException.class, task::finish);
        MatcherAssert.assertThat(
            "rejected transition cannot leave the status untouched",
            task.status(),
            Matchers.equalTo(TaskStatus.DONE)
        );
    }
}
