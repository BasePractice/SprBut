package ru.sprbut.m18;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайды 156–172 (СХЕМА 11): последовательность запуска Spring Boot")
class StartupSequenceTest {

    @BeforeEach
    void clearLog() {
        StartupLog.clear();
    }

    @Nested
    @DisplayName("События идут в строгом порядке")
    class EventOrder {

        @Test
        @DisplayName("Полная последовательность от Starting до Ready")
        void fullSequence() {
            try (ConfigurableApplicationContext context = StartupApp.run()) {
                assertThat(context.isActive()).isTrue();

                assertThat(StartupLog.events())
                        .filteredOn(e -> e.contains("Event") || e.contains("Initializer")
                                || e.contains("Runner") || e.contains("PostProcessor"))
                        .extracting(e -> e.split(":")[0])
                        .containsExactly(
                                "1-ApplicationStartingEvent",
                                "2-ApplicationEnvironmentPreparedEvent",
                                "3-ApplicationContextInitializer",
                                "4-ApplicationContextInitializedEvent",
                                "5-ApplicationPreparedEvent",
                                "6-BeanFactoryPostProcessor",
                                "7-ContextRefreshedEvent",
                                "8-ApplicationStartedEvent",
                                "9a-ApplicationRunner",
                                "9b-CommandLineRunner",
                                "10-ApplicationReadyEvent");
            }
        }

        @Test
        @DisplayName("Environment готов уже ко второму событию")
        void environmentIsReadyAtStepTwo() {
            try (var ignored = StartupApp.run()) {
                assertThat(StartupLog.events()).contains("2-env-has-property:true");
            }
        }

        @Test
        @DisplayName("ApplicationReadyEvent — последнее событие успешного запуска")
        void readyIsTheLastEvent() {
            try (var ignored = StartupApp.run()) {
                assertThat(StartupLog.events().get(StartupLog.events().size() - 1))
                        .isEqualTo("10-ApplicationReadyEvent");
            }
        }

        @Test
        @DisplayName("Раннеры выполняются между Started и Ready")
        void runnersRunBetweenStartedAndReady() {
            try (var ignored = StartupApp.run()) {
                int started = StartupLog.indexOf("8-ApplicationStartedEvent");
                int ready = StartupLog.indexOf("10-ApplicationReadyEvent");
                int runner = indexOfPrefix("9a-ApplicationRunner");

                assertThat(runner).isGreaterThan(started).isLessThan(ready);
            }
        }

        @Test
        @DisplayName("@Order задаёт порядок раннеров между собой")
        void runnersRespectOrder() {
            try (var ignored = StartupApp.run()) {
                assertThat(indexOfPrefix("9a-ApplicationRunner"))
                        .isLessThan(indexOfPrefix("9b-CommandLineRunner"));
            }
        }

        private int indexOfPrefix(String prefix) {
            var events = StartupLog.events();
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).startsWith(prefix)) {
                    return i;
                }
            }
            throw new AssertionError("Нет события с префиксом " + prefix + " в " + events);
        }
    }

    @Nested
    @DisplayName("Хуки между событиями")
    class Hooks {

        @Test
        @DisplayName("ApplicationContextInitializer успевает добавить источник настроек")
        void initializerCanAddPropertySource() {
            try (ConfigurableApplicationContext context = StartupApp.run()) {
                assertThat(context.getEnvironment().getProperty("sprbut.startup.injected"))
                        .isEqualTo("да");
            }
        }

        @Test
        @DisplayName("BeanFactoryPostProcessor видит определения бинов до их создания")
        void beanFactoryPostProcessorSeesDefinitions() {
            try (var ignored = StartupApp.run()) {
                assertThat(StartupLog.events())
                        .anyMatch(e -> e.startsWith("6-BeanFactoryPostProcessor:определений=")
                                && !e.endsWith("=0"));
            }
        }

        @Test
        @DisplayName("ApplicationRunner получает разобранные аргументы командной строки")
        void applicationRunnerReceivesParsedArguments() {
            try (var ignored = StartupApp.run("--sprbut.demo=1", "простой-аргумент")) {
                assertThat(StartupLog.events())
                        .anyMatch(e -> e.startsWith("9a-ApplicationRunner:опции=[sprbut.demo]"));
                assertThat(StartupLog.events())
                        .anyMatch(e -> e.equals("9b-CommandLineRunner:аргументов=2"));
            }
        }

        @Test
        @DisplayName("Аргумент командной строки перекрывает application.yaml — высший приоритет")
        void commandLineArgumentsHaveTopPriority() {
            try (ConfigurableApplicationContext context =
                         StartupApp.run("--sprbut.startup.marker=из аргументов")) {
                assertThat(context.getEnvironment().getProperty("sprbut.startup.marker"))
                        .isEqualTo("из аргументов");
            }
        }
    }

    @Nested
    @DisplayName("Неудачный запуск")
    class Failure {

        @Test
        @DisplayName("ApplicationFailedEvent приходит вместо ApplicationReadyEvent")
        void failedEventReplacesReady() {
            assertThatThrownBy(StartupApp::runFailing)
                    .isInstanceOf(org.springframework.beans.factory.BeanCreationException.class);

            assertThat(StartupLog.events())
                    .anyMatch(e -> e.startsWith("x-ApplicationFailedEvent"))
                    .doesNotContain("10-ApplicationReadyEvent");
        }
    }
}
