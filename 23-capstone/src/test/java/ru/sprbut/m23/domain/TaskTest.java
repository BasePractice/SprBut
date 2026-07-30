package ru.sprbut.m23.domain;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Домен: правила переходов живут в самой задаче")
final class TaskTest {

    @Test
    @DisplayName("новая задача открыта")
    void opensInOpenStatus() {
        assertThat(
            "fresh task cannot start as open",
            new Task("сверстать отчёт", Instant.parse("2026-07-30T10:00:00Z")).status(),
            equalTo(TaskStatus.OPEN)
        );
    }

    @Test
    @DisplayName("открытая задача переходит в работу")
    void movesToInProgress() {
        Task task = new Task("собрать логи", Instant.parse("2026-07-30T10:00:00Z"));
        task.start();
        assertThat(
            "open task cannot move to in progress",
            task.status(),
            equalTo(TaskStatus.IN_PROGRESS)
        );
    }

    @Test
    @DisplayName("закрытая задача не открывается заново")
    void dontReopenFinishedTask() {
        Task task = new Task("выкатить релиз", Instant.parse("2026-07-30T10:00:00Z"));
        task.start();
        task.finish();
        assertThat(
            "finished task cannot refuse to go backwards",
            assertThrows(IllegalStateException.class, task::start).getMessage(),
            containsString("DONE")
        );
    }

    @Test
    @DisplayName("отказ перехода не меняет состояние задачи")
    void keepsStatusOnRejectedMove() {
        Task task = new Task("починить сборку", Instant.parse("2026-07-30T10:00:00Z"));
        task.finish();
        assertThrows(IllegalStateException.class, task::finish);
        assertThat(
            "rejected transition cannot leave the status untouched",
            task.status(),
            equalTo(TaskStatus.DONE)
        );
    }
}
