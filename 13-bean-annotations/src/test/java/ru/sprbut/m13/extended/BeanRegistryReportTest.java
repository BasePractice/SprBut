package ru.sprbut.m13.extended;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m13.conditional.ConditionalConfig;
import ru.sprbut.m13.qualifiers.QualifierConfig;
import ru.sprbut.m13.scopes.ScopeConfig;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расширенный пример: диагностический отчёт о контейнере")
class BeanRegistryReportTest {

    @Nested
    @DisplayName("Что лежит в контейнере")
    class Contents {

        @Test
        @DisplayName("Отчёт перечисляет прикладные бины с их типами")
        void listsApplicationBeans() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(BeanRegistryReport.applicationBeans(context))
                        .extracting(BeanRegistryReport.Entry::name)
                        .contains("cardGateway", "cashGateway", "sbpGateway", "gatewayRegistry");
            }
        }

        @Test
        @DisplayName("Скоуп каждого бина виден в отчёте")
        void showsScopes() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                assertThat(BeanRegistryReport.of(context))
                        .filteredOn(e -> e.name().equals("prototypeBean"))
                        .singleElement()
                        .satisfies(e -> {
                            assertThat(e.scope()).isEqualTo("prototype");
                            assertThat(e.singleton()).isFalse();
                        });

                assertThat(BeanRegistryReport.scopeSummary(context))
                        .containsKeys("singleton", "prototype");
            }
        }

        @Test
        @DisplayName("@Primary виден в отчёте")
        void showsPrimary() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(BeanRegistryReport.of(context))
                        .filteredOn(BeanRegistryReport.Entry::primary)
                        .extracting(BeanRegistryReport.Entry::name)
                        .containsExactly("cardGateway");
            }
        }

        @Test
        @DisplayName("@DependsOn виден как явно заданный порядок")
        void showsDependsOn() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(BeanRegistryReport.of(context))
                        .filteredOn(e -> e.name().equals("cacheWarmer"))
                        .singleElement()
                        .satisfies(e -> assertThat(e.dependsOn()).containsExactly("schemaInitializer"));
            }
        }
    }

    @Nested
    @DisplayName("Диагностика типичных вопросов")
    class Diagnostics {

        @Test
        @DisplayName("«Почему внедрился не тот бин» — отчёт называет причину")
        void explainsWhichBeanWins() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(BeanRegistryReport.explainResolution(
                        context, QualifierConfig.PaymentGateway.class))
                        .startsWith("@Primary: cardGateway");
            }
        }

        @Test
        @DisplayName("«Кандидатов несколько, @Primary нет» — отчёт предсказывает падение")
        void predictsNoUniqueBeanDefinition() {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.registerBean("first", String.class, () -> "a");
                context.registerBean("second", String.class, () -> "b");
                context.refresh();

                assertThat(BeanRegistryReport.explainResolution(context, String.class))
                        .contains("@Primary нет")
                        .contains("@Qualifier");
            }
        }

        @Test
        @DisplayName("«Кандидатов нет» — отчёт называет будущее исключение")
        void predictsNoSuchBeanDefinition() {
            try (var context = new AnnotationConfigApplicationContext(ScopeConfig.class)) {
                assertThat(BeanRegistryReport.explainResolution(context, java.time.LocalDate.class))
                        .contains("нет кандидатов")
                        .contains("NoSuchBeanDefinitionException");
            }
        }

        @Test
        @DisplayName("«Почему бина нет вовсе» — его нет в отчёте: условие не выполнилось")
        void missingBeanIsSimplyAbsent() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(BeanRegistryReport.applicationBeans(context))
                        .extracting(BeanRegistryReport.Entry::name)
                        .doesNotContain("featureBean", "devOnlyBean")
                        .contains("notDevBean");
            }
        }

        @Test
        @DisplayName("«Что ещё не создано» — ленивые бины и прототипы")
        void showsWhatIsNotInstantiatedYet() {
            try (var context = new AnnotationConfigApplicationContext(ConditionalConfig.class)) {
                assertThat(BeanRegistryReport.notYetInstantiated(context)).contains("lazyBean");

                context.getBean("lazyBean");

                assertThat(BeanRegistryReport.notYetInstantiated(context))
                        .doesNotContain("lazyBean");
            }
        }

        @Test
        @DisplayName("Список кандидатов по типу — то, что видит контейнер в точке внедрения")
        void listsCandidatesByType() {
            try (var context = new AnnotationConfigApplicationContext(QualifierConfig.class)) {
                assertThat(BeanRegistryReport.candidatesFor(
                        context, QualifierConfig.PaymentGateway.class))
                        .containsExactly("cardGateway", "cashGateway", "sbpGateway");
            }
        }
    }
}
