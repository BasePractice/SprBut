package ru.sprbut.m18.extended;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m18.StartupApp;
import ru.sprbut.m18.StartupLog;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: СХЕМА 11 — восстановленная диаграмма запуска")
class StartupTimelineTest {

    @BeforeEach
    void clearLog() {
        StartupLog.clear();
    }

    @Test
    @DisplayName("Фактическая последовательность соответствует канонической")
    void actualSequenceMatchesTheCanonicalOne() {
        try (var ignored = StartupApp.run()) {
            assertThat(StartupTimeline.actualSequence())
                    .containsSubsequence(
                            "ApplicationStartingEvent",
                            "ApplicationEnvironmentPreparedEvent",
                            "ApplicationContextInitializer",
                            "ApplicationContextInitializedEvent",
                            "ApplicationPreparedEvent",
                            "BeanFactoryPostProcessor",
                            "ContextRefreshedEvent",
                            "ApplicationStartedEvent",
                            "ApplicationRunner",
                            "CommandLineRunner",
                            "ApplicationReadyEvent");
        }
    }

    @Test
    @DisplayName("Номера шагов только возрастают — порядок не нарушен")
    void orderNeverDecreases() {
        try (var ignored = StartupApp.run()) {
            assertThat(StartupTimeline.isOrdered()).isTrue();
            assertThat(StartupTimeline.actualOrder())
                    .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        }
    }

    @Test
    @DisplayName("Диаграмма читается и годится для разбора старта")
    void rendersReadableDiagram() {
        try (var ignored = StartupApp.run()) {
            assertThat(StartupTimeline.render())
                    .startsWith("SpringApplication.run()")
                    .contains("1-ApplicationStartingEvent")
                    .endsWith("▼ приложение готово");
        }
    }

    @Test
    @DisplayName("Справочник точек расширения покрывает все десять шагов")
    void hookCatalogCoversEveryStep() {
        assertThat(StartupTimeline.whereToHook())
                .hasSize(10)
                .extracting(StartupTimeline.HookPoint::order)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("Для каждой точки сказано, что готово и что здесь делают")
    void everyHookExplainsWhatIsReady() {
        assertThat(StartupTimeline.hook("ApplicationStartingEvent"))
                .get()
                .satisfies(hook -> {
                    assertThat(hook.whatIsReady()).contains("ничего");
                    assertThat(hook.typicalUse()).contains("логирования");
                });

        assertThat(StartupTimeline.hook("ApplicationReadyEvent"))
                .get()
                .satisfies(hook -> assertThat(hook.whatIsReady()).contains("готово всё"));
    }

    @Test
    @DisplayName("BeanFactoryPostProcessor работает с определениями, а не с объектами")
    void beanFactoryPostProcessorPrecedesBeanCreation() {
        assertThat(StartupTimeline.hook("BeanFactoryPostProcessor"))
                .get()
                .satisfies(hook -> {
                    assertThat(hook.whatIsReady()).contains("определения");
                    assertThat(hook.order()).isLessThan(
                            StartupTimeline.hook("ContextRefreshedEvent").orElseThrow().order());
                });
    }

    @Test
    @DisplayName("Каждый шаг успешного запуска встречается ровно один раз")
    void everyStepHappensOnce() {
        try (var ignored = StartupApp.run()) {
            assertThat(StartupTimeline.counts())
                    .containsEntry("ApplicationStartingEvent", 1L)
                    .containsEntry("ApplicationReadyEvent", 1L)
                    .containsEntry("ContextRefreshedEvent", 1L);
        }
    }
}
