package ru.sprbut.m19.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration;
import ru.sprbut.m19.greeter.Greeter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: отчёт об условиях (то, что печатает --debug)")
class ConditionReportTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreeterAutoConfiguration.class));

    @Configuration
    static class UserGreeterConfig {

        @Bean
        Greeter greeter() {
            return new Greeter() {
                @Override
                public String greet(String name) {
                    return "своё приветствие " + name;
                }

                @Override
                public String flavour() {
                    return "пользовательский";
                }
            };
        }
    }

    @Test
    @DisplayName("Применённая автоконфигурация попадает в список включённых")
    void appliedConfigurationIsReported() {
        runner.run(context -> {
            var configurable = (ConfigurableApplicationContext) context.getSourceApplicationContext();

            assertThat(ConditionReport.isApplied(configurable, "GreeterAutoConfiguration")).isTrue();
            assertThat(ConditionReport.included(configurable))
                    .anyMatch(name -> name.contains("GreeterAutoConfiguration"));
        });
    }

    @Test
    @DisplayName("«Почему бина нет» — отчёт называет невыполненное условие")
    void explainsWhyBeanIsMissing() {
        runner.withPropertyValues("sprbut.greeter.enabled=false").run(context -> {
            var configurable = (ConfigurableApplicationContext) context.getSourceApplicationContext();

            assertThat(context).doesNotHaveBean(Greeter.class);
            assertThat(ConditionReport.whyExcluded(configurable, "GreeterAutoConfiguration"))
                    .get()
                    .asString()
                    .contains("sprbut.greeter.enabled");
        });
    }

    @Test
    @DisplayName("«Почему бина нет» при пользовательском бине — сработал @ConditionalOnMissingBean")
    void explainsBackOff() {
        runner.withUserConfiguration(UserGreeterConfig.class).run(context -> {
            var configurable = (ConfigurableApplicationContext) context.getSourceApplicationContext();

            assertThat(ConditionReport.render(configurable, "GreeterAutoConfiguration"))
                    .contains("GreeterAutoConfiguration");
            assertThat(context.getBean(Greeter.class).flavour()).isEqualTo("пользовательский");
        });
    }

    @Test
    @DisplayName("Отчёт формулирует условия дословно — как в выводе --debug")
    void reasonsAreVerbatim() {
        runner.run(context -> {
            var configurable = (ConfigurableApplicationContext) context.getSourceApplicationContext();

            assertThat(ConditionReport.matching(configurable, "GreeterAutoConfiguration"))
                    .isNotEmpty()
                    .allSatisfy((source, entry) -> assertThat(entry.reasons()).isNotEmpty());
        });
    }

    @Test
    @DisplayName("Наглядный отчёт годится для вставки в лог")
    void rendersHumanReadableReport() {
        runner.run(context -> {
            var configurable = (ConfigurableApplicationContext) context.getSourceApplicationContext();

            assertThat(ConditionReport.render(configurable, "GreeterAutoConfiguration"))
                    .startsWith("Отчёт об условиях для 'GreeterAutoConfiguration':")
                    .contains("ПРИМЕНЕНА");
        });
    }

    @Test
    @DisplayName("Несуществующая конфигурация — честный ответ, а не пустота")
    void unknownConfigurationIsReported() {
        runner.run(context -> {
            var configurable = (ConfigurableApplicationContext) context.getSourceApplicationContext();

            assertThat(ConditionReport.render(configurable, "НетТакойКонфигурации"))
                    .contains("нет подходящих конфигураций");
            assertThat(ConditionReport.isApplied(configurable, "НетТакойКонфигурации")).isFalse();
        });
    }
}
