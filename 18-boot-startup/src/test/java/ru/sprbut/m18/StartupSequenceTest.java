/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m18;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайды 156–172 (СХЕМА 11): последовательность запуска Spring Boot.
 * @since 1.0
 */
@DisplayName("Слайды 156–172 (СХЕМА 11): последовательность запуска Spring Boot")
final class StartupSequenceTest {
    @Test
    @DisplayName("запуск начинается с ApplicationStartingEvent")
    void startsWithStartingEvent() {
        MatcherAssert.assertThat(
            "startup cannot begin with the starting event",
            started().events().get(0),
            Matchers.containsString("ApplicationStartingEvent")
        );
    }

    @Test
    @DisplayName("Environment готов уже ко второму событию")
    void preparesEnvironmentEarly() {
        MatcherAssert.assertThat(
            "environment cannot be ready by the second event",
            started().events(),
            Matchers.hasItem("2-env-has-property:true")
        );
    }

    @Test
    @DisplayName("ApplicationReadyEvent — последнее событие успешного запуска")
    void endsWithReadyEvent() {
        final StartupLog log = started();
        MatcherAssert.assertThat(
            "ready event cannot close the successful startup",
            log.events().get(log.events().size() - 1),
            Matchers.containsString("ApplicationReadyEvent")
        );
    }

    @Test
    @DisplayName("раннеры выполняются между Started и Ready")
    void runsRunnersBetweenStartedAndReady() {
        final StartupLog log = started();
        MatcherAssert.assertThat(
            "runners cannot run between started and ready",
            log.indexOf("8-ApplicationStartedEvent"),
            Matchers.lessThan(log.indexOf("10-ApplicationReadyEvent"))
        );
    }

    @Test
    @DisplayName("BeanFactoryPostProcessor видит определения бинов до их создания")
    void seesDefinitionsBeforeInstances() {
        MatcherAssert.assertThat(
            "post processor cannot see the definitions first",
            started().events().stream().anyMatch(e -> e.contains("BeanFactoryPostProcessor")),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("ApplicationContextInitializer вызывается до загрузки бинов")
    void initializesContextBeforeBeans() {
        final StartupLog log = started();
        MatcherAssert.assertThat(
            "initializer cannot run before the bean definitions are loaded",
            log.indexOf("3-ApplicationContextInitializer"),
            Matchers.lessThan(log.indexOf("5-ApplicationPreparedEvent"))
        );
    }

    @Test
    @DisplayName("неудачный запуск даёт ApplicationFailedEvent вместо Ready")
    void reportsFailureInsteadOfReady() {
        final StartupLog log = new StartupLog();
        log.clear();
        try {
            StartupApp.runFailing();
        } catch (final RuntimeException expected) {
            MatcherAssert.assertThat(
                "failed startup cannot publish the failure event",
                log.events().stream().anyMatch(e -> e.contains("Failed")),
                Matchers.equalTo(true)
            );
        }
    }

    private static StartupLog started(final String... args) {
        final StartupLog log = new StartupLog();
        log.clear();
        StartupApp.run(args).close();
        return log;
    }
}
