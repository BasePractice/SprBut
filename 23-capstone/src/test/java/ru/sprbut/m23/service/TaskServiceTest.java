/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m23.service;

import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.sprbut.m23.audit.AuditTrail;
import ru.sprbut.m23.domain.TaskStatus;

/**
 * Сервисный слой: полный контекст с зафиксированным временем.
 * @since 1.0
 */
@SpringBootTest
@TestPropertySource(properties = "tracker.limit=2")
@Transactional
@DisplayName("Сервисный слой: полный контекст с зафиксированным временем")
final class TaskServiceTest {

    /**
     * Задачи.
     */
    @Autowired
    private Tasks tasks;

    /**
     * Журнал событий.
     */
    @Autowired
    private AuditTrail trail;

    @Test
    @DisplayName("новая задача получает время из внедрённых часов")
    void stampsCreationTimeFromClock() {
        MatcherAssert.assertThat(
            "injected clock cannot define the creation time",
            this.tasks.open("написать тесты").created(),
            Matchers.equalTo(Instant.parse("2026-07-30T10:15:30Z"))
        );
    }

    @Test
    @DisplayName("задача сохраняется и находится по состоянию")
    void findsTaskByStatus() {
        this.tasks.open("проверить логи");
        MatcherAssert.assertThat(
            "saved task cannot be found by its status",
            this.tasks.byStatus(TaskStatus.OPEN),
            Matchers.hasSize(1)
        );
    }

    @Test
    @DisplayName("лимит открытых задач берётся из конфигурации")
    void enforcesConfiguredLimit() {
        this.tasks.open("первая");
        this.tasks.open("вторая");
        MatcherAssert.assertThat(
            "configured limit cannot stop the third task",
            Assertions.assertThrows(
                IllegalStateException.class, () -> this.tasks.open("третья")
            ).getMessage(),
            Matchers.containsString("лимит")
        );
    }

    @Test
    @DisplayName("несуществующая задача не переводится в работу")
    void dontStartUnknownTask() {
        MatcherAssert.assertThat(
            "unknown task cannot be reported as missing",
            Assertions.assertThrows(
                IllegalArgumentException.class, () -> this.tasks.start(4242L)
            ).getMessage(),
            Matchers.containsString("4242")
        );
    }

    @Test
    @DisplayName("операции сервиса попадают в журнал аудита через аспект")
    void recordsAuditedOperations() {
        this.tasks.open("собрать сборку");
        MatcherAssert.assertThat(
            "aspect cannot record the open operation",
            this.trail.records(),
            Matchers.hasItem("task.open")
        );
    }

    @Test
    @DisplayName("метод без аннотации в журнал не попадает")
    void dontRecordUnannotatedOperations() {
        final int before = this.trail.records().size();
        this.tasks.byStatus(TaskStatus.OPEN);
        MatcherAssert.assertThat(
            "unannotated lookup cannot leave the trail untouched",
            this.trail.records().size(),
            Matchers.equalTo(before)
        );
    }

    @Test
    @DisplayName("закрытие задачи проходит через все переходы")
    void finishesTaskThroughStages() {
        final long id = this.tasks.open("выкатить релиз").id();
        this.tasks.start(id);
        MatcherAssert.assertThat(
            "started task cannot be finished",
            this.tasks.finish(id).status(),
            Matchers.equalTo(TaskStatus.DONE)
        );
    }

    @Test
    @DisplayName("сервис приходит в тест обёрнутым в JDK-прокси, а не голым классом")
    void arrivesAsJdkProxy() {
        MatcherAssert.assertThat(
            "service with an interface cannot arrive as a JDK proxy",
            Proxy.isProxyClass(this.tasks.getClass()),
            Matchers.equalTo(true)
        );
    }

    /**
     * Часы подменяются собственным бином: {@code @ConditionalOnMissingBean}
     * в конфигурации отступает, и время в тесте перестаёт быть случайным.
     * @since 1.0
     */
    @TestConfiguration
    @SuppressWarnings("PMD.JUnitTestClassShouldBeFinal")
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-07-30T10:15:30Z"), ZoneOffset.UTC);
        }
    }
}
