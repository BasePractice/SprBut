/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m13.extended;

import java.time.LocalDate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m13.qualifiers.QualifierConfig;
import ru.sprbut.m13.scopes.ScopeConfig;

/**
 * Расширенный пример: диагностический отчёт о контейнере.
 * @since 1.0
 */
@DisplayName("Расширенный пример: диагностический отчёт о контейнере")
final class BeanRegistryReportTest {

    @Test
    @DisplayName("отчёт перечисляет прикладные бины")
    void listsApplicationBeans() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            MatcherAssert.assertThat(
                "report cannot list the application beans",
                new BeanRegistryReport(context).application().stream().map(Entry::name).toList(),
                Matchers.hasItem("singletonBean")
            );
        }
    }

    @Test
    @DisplayName("инфраструктура Spring в прикладной отчёт не попадает")
    void hidesSpringInfrastructure() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            MatcherAssert.assertThat(
                "Spring internals cannot stay out of the application report",
                new BeanRegistryReport(context).application().stream().map(Entry::name).toList(),
                Matchers.not(Matchers.hasItem(Matchers.containsString("org.springframework")))
            );
        }
    }

    @Test
    @DisplayName("сводка по скоупам считает прототипы отдельно")
    void countsScopes() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            MatcherAssert.assertThat(
                "scope summary cannot count the prototypes",
                new BeanRegistryReport(context).scopes(),
                Matchers.hasKey("prototype")
            );
        }
    }

    @Test
    @DisplayName("«почему внедрился не тот бин» — отчёт называет @Primary")
    void explainsPrimaryWinner() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(QualifierConfig.class)) {
            MatcherAssert.assertThat(
                "report cannot explain the primary winner",
                new BeanRegistryReport(context).resolution(
                    ru.sprbut.m13.qualifiers.QualifierConfig.PaymentGateway.class
                ),
                Matchers.containsString("@Primary")
            );
        }
    }

    @Test
    @DisplayName("«кандидатов нет» — отчёт называет будущее исключение")
    void predictsMissingBean() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            MatcherAssert.assertThat(
                "report cannot predict NoSuchBeanDefinitionException",
                new BeanRegistryReport(context).resolution(LocalDate.class),
                Matchers.containsString("NoSuchBeanDefinitionException")
            );
        }
    }

    @Test
    @DisplayName("прототип в отчёте числится несозданным — экземпляров у него нет")
    void reportsPrototypeAsPending() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            MatcherAssert.assertThat(
                "prototype cannot be reported as pending",
                new BeanRegistryReport(context).pending(),
                Matchers.hasItem("prototypeBean")
            );
        }
    }
}
