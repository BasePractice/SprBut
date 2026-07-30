package ru.sprbut.m23.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = "tracker.limit=2")
@Transactional
@DisplayName("Сервисный слой: полный контекст с зафиксированным временем")
final class TaskServiceTest {

    /**
     * Часы подменяются собственным бином: {@code @ConditionalOnMissingBean}
     * в конфигурации отступает, и время в тесте перестаёт быть случайным.
     */
    @TestConfiguration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-07-30T10:15:30Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private Tasks tasks;

    @Autowired
    private AuditTrail trail;

    @Test
    @DisplayName("новая задача получает время из внедрённых часов")
    void stampsCreationTimeFromClock() {
        assertThat(
            "injected clock cannot define the creation time",
            this.tasks.open("написать тесты").created(),
            equalTo(Instant.parse("2026-07-30T10:15:30Z"))
        );
    }

    @Test
    @DisplayName("задача сохраняется и находится по состоянию")
    void findsTaskByStatus() {
        this.tasks.open("проверить логи");
        assertThat(
            "saved task cannot be found by its status",
            this.tasks.byStatus(TaskStatus.OPEN),
            hasSize(1)
        );
    }

    @Test
    @DisplayName("лимит открытых задач берётся из конфигурации")
    void enforcesConfiguredLimit() {
        this.tasks.open("первая");
        this.tasks.open("вторая");
        assertThat(
            "configured limit cannot stop the third task",
            assertThrows(IllegalStateException.class, () -> this.tasks.open("третья")).getMessage(),
            containsString("лимит")
        );
    }

    @Test
    @DisplayName("несуществующая задача не переводится в работу")
    void dontStartUnknownTask() {
        assertThat(
            "unknown task cannot be reported as missing",
            assertThrows(IllegalArgumentException.class, () -> this.tasks.start(4242L)).getMessage(),
            containsString("4242")
        );
    }

    @Test
    @DisplayName("операции сервиса попадают в журнал аудита через аспект")
    void recordsAuditedOperations() {
        this.tasks.open("собрать сборку");
        assertThat(
            "aspect cannot record the open operation",
            this.trail.records(),
            hasItem("task.open")
        );
    }

    @Test
    @DisplayName("метод без аннотации в журнал не попадает")
    void dontRecordUnannotatedOperations() {
        int before = this.trail.records().size();
        this.tasks.byStatus(TaskStatus.OPEN);
        assertThat(
            "unannotated lookup cannot leave the trail untouched",
            this.trail.records().size(),
            equalTo(before)
        );
    }

    @Test
    @DisplayName("закрытие задачи проходит через все переходы")
    void finishesTaskThroughStages() {
        long id = this.tasks.open("выкатить релиз").id();
        this.tasks.start(id);
        assertThat(
            "started task cannot be finished",
            this.tasks.finish(id).status(),
            equalTo(TaskStatus.DONE)
        );
    }

    @Test
    @DisplayName("сервис приходит в тест обёрнутым в JDK-прокси, а не голым классом")
    void arrivesAsJdkProxy() {
        assertThat(
            "service with an interface cannot arrive as a JDK proxy",
            java.lang.reflect.Proxy.isProxyClass(this.tasks.getClass()),
            equalTo(true)
        );
    }
}
