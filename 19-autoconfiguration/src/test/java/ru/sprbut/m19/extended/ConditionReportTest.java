/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m19.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m19.autoconfigure.GreeterAutoConfiguration;
import ru.sprbut.m19.greeter.Greeter;

/**
 * Расширенный пример: отчёт об условиях (то, что печатает --debug).
 * @since 1.0
 */
@DisplayName("Расширенный пример: отчёт об условиях (то, что печатает --debug)")
final class ConditionReportTest {

    @Test
    @DisplayName("применённая автоконфигурация попадает в список включённых")
    void listsAppliedConfiguration() {
        runner().run(context -> MatcherAssert.assertThat(
            "applied auto configuration cannot be listed as included",
            new ConditionReport((ConfigurableApplicationContext) context)
                .included().stream().anyMatch(name -> name.contains("GreeterAutoConfiguration")),
            Matchers.equalTo(true
)
        ));
    }

    @Test
    @DisplayName("отчёт подтверждает, что конфигурация применилась")
    void confirmsApplication() {
        runner().run(context -> MatcherAssert.assertThat(
            "report cannot confirm the applied configuration",
            new ConditionReport((ConfigurableApplicationContext) context)
                .applied("GreeterAutoConfiguration"),
            Matchers.equalTo(true
)
        ));
    }

    @Test
    @DisplayName("«почему бина нет» — отчёт называет невыполненное условие")
    void namesFailedCondition() {
        runner().withPropertyValues("sprbut.greeter.enabled=false").run(context -> MatcherAssert.assertThat(
            "report cannot name the failed condition",
            new ConditionReport((ConfigurableApplicationContext) context)
                .whyExcluded("GreeterAutoConfiguration").orElse(""),
            Matchers.containsString("sprbut.greeter.enabled"
)
        ));
    }

    @Test
    @DisplayName("свой бин отменяет автоконфигурацию — сработал @ConditionalOnMissingBean")
    void yieldsToUserBean() {
        runner().withUserConfiguration(OwnGreeterConfig.class).run(context -> MatcherAssert.assertThat(
            "auto configuration cannot yield to the user bean",
            new ConditionReport((ConfigurableApplicationContext) context)
                .render("GreeterAutoConfiguration"),
            Matchers.containsString("Greeter"
)
        ));
    }

    @Test
    @DisplayName("отчёт формулирует условия дословно, как вывод --debug")
    void quotesConditionsVerbatim() {
        runner().run(context -> MatcherAssert.assertThat(
            "report cannot quote the conditions verbatim",
            new ConditionReport((ConfigurableApplicationContext) context)
                .matching("GreeterAutoConfiguration").keySet().stream()
                .anyMatch(name -> name.contains("GreeterAutoConfiguration")),
            Matchers.equalTo(true
)
        ));
    }

    @Test
    @DisplayName("наглядный отчёт годится для вставки в лог")
    void rendersReadableReport() {
        runner().run(context -> MatcherAssert.assertThat(
            "report cannot be rendered readably",
            new ConditionReport((ConfigurableApplicationContext) context)
                .render("GreeterAutoConfiguration"),
            Matchers.containsString("Отчёт об условиях"
)
        ));
    }

    @Test
    @DisplayName("несуществующая конфигурация получает честный ответ, а не пустоту")
    void answersAboutUnknownConfiguration() {
        runner().run(context -> MatcherAssert.assertThat(
            "unknown configuration cannot get an honest answer",
            new ConditionReport((ConfigurableApplicationContext) context)
                .render("НетТакойКонфигурации"),
            Matchers.containsString("нет подходящих конфигураций"
)
        ));
    }

    @Test
    @DisplayName("несуществующая конфигурация применённой не считается")
    void dontCallUnknownConfigurationApplied() {
        runner().run(context -> MatcherAssert.assertThat(
            "unknown configuration cannot avoid the applied verdict",
            new ConditionReport((ConfigurableApplicationContext) context)
                .applied("НетТакойКонфигурации"),
            Matchers.equalTo(false
)
        ));
    }

    private static ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreeterAutoConfiguration.class));
    }

    @Configuration(proxyBeanMethods = false)
    static final class OwnGreeterConfig {

        @Bean
        Greeter greeter() {
            return new Greeter() {
                @Override
                public String greet(final String name) {
                    return String.format("своё приветствие %s", name);
                }

                @Override
                public String flavour() {
                    return "собственный";
                }
            };
        }
    }
}
