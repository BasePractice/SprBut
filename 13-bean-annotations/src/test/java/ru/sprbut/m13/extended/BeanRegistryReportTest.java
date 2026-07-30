package ru.sprbut.m13.extended;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m13.qualifiers.QualifierConfig;
import ru.sprbut.m13.scopes.ScopeConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

@DisplayName("Расширенный пример: диагностический отчёт о контейнере")
final class BeanRegistryReportTest {

    @Test
    @DisplayName("отчёт перечисляет прикладные бины")
    void listsApplicationBeans() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            assertThat(
                "report cannot list the application beans",
                new BeanRegistryReport(context).application().stream().map(Entry::name).toList(),
                hasItem("singletonBean")
            );
        }
    }

    @Test
    @DisplayName("инфраструктура Spring в прикладной отчёт не попадает")
    void hidesSpringInfrastructure() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            assertThat(
                "Spring internals cannot stay out of the application report",
                new BeanRegistryReport(context).application().stream().map(Entry::name).toList(),
                not(hasItem(containsString("org.springframework")))
            );
        }
    }

    @Test
    @DisplayName("сводка по скоупам считает прототипы отдельно")
    void countsScopes() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            assertThat(
                "scope summary cannot count the prototypes",
                new BeanRegistryReport(context).scopes(),
                hasKey("prototype")
            );
        }
    }

    @Test
    @DisplayName("«почему внедрился не тот бин» — отчёт называет @Primary")
    void explainsPrimaryWinner() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(QualifierConfig.class)) {
            assertThat(
                "report cannot explain the primary winner",
                new BeanRegistryReport(context).resolution(
                    ru.sprbut.m13.qualifiers.QualifierConfig.PaymentGateway.class
                ),
                containsString("@Primary")
            );
        }
    }

    @Test
    @DisplayName("«кандидатов нет» — отчёт называет будущее исключение")
    void predictsMissingBean() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            assertThat(
                "report cannot predict NoSuchBeanDefinitionException",
                new BeanRegistryReport(context).resolution(LocalDate.class),
                containsString("NoSuchBeanDefinitionException")
            );
        }
    }

    @Test
    @DisplayName("прототип в отчёте числится несозданным — экземпляров у него нет")
    void reportsPrototypeAsPending() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ScopeConfig.class)) {
            assertThat(
                "prototype cannot be reported as pending",
                new BeanRegistryReport(context).pending(),
                hasItem("prototypeBean")
            );
        }
    }
}
