package ru.sprbut.m18.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m18.StartupApp;
import ru.sprbut.m18.StartupLog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

@DisplayName("Расширенный пример: СХЕМА 11 — восстановленная диаграмма запуска")
final class StartupTimelineTest {

    private static StartupTimeline started() {
        new StartupLog().clear();
        StartupApp.run().close();
        return new StartupTimeline();
    }

    @Test
    @DisplayName("фактическая последовательность восстанавливается из журнала")
    void restoresActualSequence() {
        assertThat(
            "actual sequence cannot be restored from the log",
            started().actualSequence(),
            hasItem(containsString("ApplicationReadyEvent"))
        );
    }

    @Test
    @DisplayName("номера шагов только возрастают — порядок не нарушен")
    void keepsStepsOrdered() {
        assertThat(
            "step numbers cannot grow monotonically",
            started().isOrdered(),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("десятый шаг разбирается как 10, а не как 1")
    void parsesTwoDigitStep() {
        assertThat(
            "two digit step cannot be parsed correctly",
            started().actualOrder(),
            hasItem(10)
        );
    }

    @Test
    @DisplayName("диаграмма читается и годится для разбора старта")
    void rendersDiagram() {
        assertThat(
            "diagram cannot be rendered readably",
            started().render(),
            containsString("SpringApplication.run()")
        );
    }

    @Test
    @DisplayName("справочник точек расширения покрывает все десять шагов")
    void coversEveryHookPoint() {
        assertThat(
            "hook catalogue cannot cover all ten steps",
            new StartupTimeline().whereToHook(),
            hasSize(10)
        );
    }

    @Test
    @DisplayName("справочник отвечает, что готово к моменту события")
    void explainsWhatIsReady() {
        assertThat(
            "hook point cannot explain what is ready",
            new StartupTimeline().hook("ApplicationEnvironmentPrepared").orElseThrow().ready(),
            containsString("Environment собран")
        );
    }

    @Test
    @DisplayName("сводка считает, сколько раз встретился каждый шаг")
    void countsPhases() {
        assertThat(
            "summary cannot count the phases",
            started().counts().isEmpty(),
            equalTo(false)
        );
    }
}
