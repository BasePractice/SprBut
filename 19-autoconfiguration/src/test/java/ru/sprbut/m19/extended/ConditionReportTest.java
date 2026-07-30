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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@DisplayName("Расширенный пример: отчёт об условиях (то, что печатает --debug)")
final class ConditionReportTest {

    @Configuration(proxyBeanMethods = false)
    static class OwnGreeterConfig {

        @Bean
        Greeter greeter() {
            return new Greeter() {
                @Override
                public String greet(String name) {
                    return "своё приветствие " + name;
                }

                @Override
                public String flavour() {
                    return "собственный";
                }
            };
        }
    }

    private static ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreeterAutoConfiguration.class));
    }

    @Test
    @DisplayName("применённая автоконфигурация попадает в список включённых")
    void listsAppliedConfiguration() {
        runner().run(context -> assertThat(
            "applied auto configuration cannot be listed as included",
            new ConditionReport((ConfigurableApplicationContext) context)
                .included().stream().anyMatch(name -> name.contains("GreeterAutoConfiguration")),
            equalTo(true)
        ));
    }

    @Test
    @DisplayName("отчёт подтверждает, что конфигурация применилась")
    void confirmsApplication() {
        runner().run(context -> assertThat(
            "report cannot confirm the applied configuration",
            new ConditionReport((ConfigurableApplicationContext) context)
                .applied("GreeterAutoConfiguration"),
            equalTo(true)
        ));
    }

    @Test
    @DisplayName("«почему бина нет» — отчёт называет невыполненное условие")
    void namesFailedCondition() {
        runner().withPropertyValues("sprbut.greeter.enabled=false").run(context -> assertThat(
            "report cannot name the failed condition",
            new ConditionReport((ConfigurableApplicationContext) context)
                .whyExcluded("GreeterAutoConfiguration").orElse(""),
            containsString("sprbut.greeter.enabled")
        ));
    }

    @Test
    @DisplayName("свой бин отменяет автоконфигурацию — сработал @ConditionalOnMissingBean")
    void yieldsToUserBean() {
        runner().withUserConfiguration(OwnGreeterConfig.class).run(context -> assertThat(
            "auto configuration cannot yield to the user bean",
            new ConditionReport((ConfigurableApplicationContext) context)
                .render("GreeterAutoConfiguration"),
            containsString("Greeter")
        ));
    }

    @Test
    @DisplayName("отчёт формулирует условия дословно, как вывод --debug")
    void quotesConditionsVerbatim() {
        runner().run(context -> assertThat(
            "report cannot quote the conditions verbatim",
            new ConditionReport((ConfigurableApplicationContext) context)
                .matching("GreeterAutoConfiguration").keySet().stream()
                .anyMatch(name -> name.contains("GreeterAutoConfiguration")),
            equalTo(true)
        ));
    }

    @Test
    @DisplayName("наглядный отчёт годится для вставки в лог")
    void rendersReadableReport() {
        runner().run(context -> assertThat(
            "report cannot be rendered readably",
            new ConditionReport((ConfigurableApplicationContext) context)
                .render("GreeterAutoConfiguration"),
            containsString("Отчёт об условиях")
        ));
    }

    @Test
    @DisplayName("несуществующая конфигурация получает честный ответ, а не пустоту")
    void answersAboutUnknownConfiguration() {
        runner().run(context -> assertThat(
            "unknown configuration cannot get an honest answer",
            new ConditionReport((ConfigurableApplicationContext) context)
                .render("НетТакойКонфигурации"),
            containsString("нет подходящих конфигураций")
        ));
    }

    @Test
    @DisplayName("несуществующая конфигурация применённой не считается")
    void dontCallUnknownConfigurationApplied() {
        runner().run(context -> assertThat(
            "unknown configuration cannot avoid the applied verdict",
            new ConditionReport((ConfigurableApplicationContext) context)
                .applied("НетТакойКонфигурации"),
            equalTo(false)
        ));
    }
}
