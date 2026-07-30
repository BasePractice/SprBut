package ru.sprbut.m18;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThan;

@DisplayName("Слайды 156–172 (СХЕМА 11): последовательность запуска Spring Boot")
final class StartupSequenceTest {

    private static StartupLog started(String... args) {
        StartupLog log = new StartupLog();
        log.clear();
        StartupApp.run(args).close();
        return log;
    }

    @Test
    @DisplayName("запуск начинается с ApplicationStartingEvent")
    void startsWithStartingEvent() {
        assertThat(
            "startup cannot begin with the starting event",
            started().events().get(0),
            containsString("ApplicationStartingEvent")
        );
    }

    @Test
    @DisplayName("Environment готов уже ко второму событию")
    void preparesEnvironmentEarly() {
        assertThat(
            "environment cannot be ready by the second event",
            started().events(),
            hasItem("2-env-has-property:true")
        );
    }

    @Test
    @DisplayName("ApplicationReadyEvent — последнее событие успешного запуска")
    void endsWithReadyEvent() {
        StartupLog log = started();
        assertThat(
            "ready event cannot close the successful startup",
            log.events().get(log.events().size() - 1),
            containsString("ApplicationReadyEvent")
        );
    }

    @Test
    @DisplayName("раннеры выполняются между Started и Ready")
    void runsRunnersBetweenStartedAndReady() {
        StartupLog log = started();
        assertThat(
            "runners cannot run between started and ready",
            log.indexOf("8-ApplicationStartedEvent"),
            lessThan(log.indexOf("10-ApplicationReadyEvent"))
        );
    }

    @Test
    @DisplayName("BeanFactoryPostProcessor видит определения бинов до их создания")
    void seesDefinitionsBeforeInstances() {
        assertThat(
            "post processor cannot see the definitions first",
            started().events().stream().anyMatch(e -> e.contains("BeanFactoryPostProcessor")),
            equalTo(true)
        );
    }

    @Test
    @DisplayName("ApplicationContextInitializer вызывается до загрузки бинов")
    void initializesContextBeforeBeans() {
        StartupLog log = started();
        assertThat(
            "initializer cannot run before the bean definitions are loaded",
            log.indexOf("3-ApplicationContextInitializer"),
            lessThan(log.indexOf("5-ApplicationPreparedEvent"))
        );
    }

    @Test
    @DisplayName("неудачный запуск даёт ApplicationFailedEvent вместо Ready")
    void reportsFailureInsteadOfReady() {
        StartupLog log = new StartupLog();
        log.clear();
        try {
            StartupApp.runFailing();
        } catch (RuntimeException expected) {
            assertThat(
                "failed startup cannot publish the failure event",
                log.events().stream().anyMatch(e -> e.contains("Failed")),
                equalTo(true)
            );
        }
    }
}
