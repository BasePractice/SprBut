/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m18.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m18.StartupApp;
import ru.sprbut.m18.StartupLog;

/**
 * Расширенный пример: СХЕМА 11 — восстановленная диаграмма запуска.
 * @since 1.0
 */
@DisplayName("Расширенный пример: СХЕМА 11 — восстановленная диаграмма запуска")
final class StartupTimelineTest {
    @Test
    @DisplayName("фактическая последовательность восстанавливается из журнала")
    void restoresActualSequence() {
        MatcherAssert.assertThat(
            "actual sequence cannot be restored from the log",
            started().actualSequence(),
            Matchers.hasItem(Matchers.containsString("ApplicationReadyEvent"))
        );
    }

    @Test
    @DisplayName("номера шагов только возрастают — порядок не нарушен")
    void keepsStepsOrdered() {
        MatcherAssert.assertThat(
            "step numbers cannot grow monotonically",
            started().isOrdered(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("десятый шаг разбирается как 10, а не как 1")
    void parsesTwoDigitStep() {
        MatcherAssert.assertThat(
            "two digit step cannot be parsed correctly",
            started().actualOrder(),
            Matchers.hasItem(10)
        );
    }

    @Test
    @DisplayName("диаграмма читается и годится для разбора старта")
    void rendersDiagram() {
        MatcherAssert.assertThat(
            "diagram cannot be rendered readably",
            started().render(),
            Matchers.containsString("SpringApplication.run()")
        );
    }

    @Test
    @DisplayName("справочник точек расширения покрывает все десять шагов")
    void coversEveryHookPoint() {
        MatcherAssert.assertThat(
            "hook catalogue cannot cover all ten steps",
            new StartupTimeline().whereToHook(),
            Matchers.hasSize(10)
        );
    }

    @Test
    @DisplayName("справочник отвечает, что готово к моменту события")
    void explainsWhatIsReady() {
        MatcherAssert.assertThat(
            "hook point cannot explain what is ready",
            new StartupTimeline().hook("ApplicationEnvironmentPrepared").orElseThrow().ready(),
            Matchers.containsString("Environment собран")
        );
    }

    @Test
    @DisplayName("сводка считает, сколько раз встретился каждый шаг")
    void countsPhases() {
        MatcherAssert.assertThat(
            "summary cannot count the phases",
            started().counts().isEmpty(),
            Matchers.equalTo(false)
        );
    }

    private static StartupTimeline started() {
        new StartupLog().clear();
        StartupApp.run().close();
        return new StartupTimeline();
    }
}
