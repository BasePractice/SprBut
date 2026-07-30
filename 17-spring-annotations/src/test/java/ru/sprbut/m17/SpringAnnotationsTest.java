package ru.sprbut.m17;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.sprbut.m17.conditionals.ConditionalOnDemo;
import ru.sprbut.m17.configuration.ProxyBeanMethods;
import ru.sprbut.m17.stereotypes.Stereotypes;
import ru.sprbut.m17.transactional.TransactionalDemo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайды 139–149: аннотации Spring")
class SpringAnnotationsTest {

    @Nested
    @DisplayName("Слайды 140–144: стереотипы")
    class StereotypeScanning {

        @Configuration
        @ComponentScan(basePackageClasses = Stereotypes.class)
        static class ScanConfig {
        }

        @Test
        @DisplayName("Сканер находит все четыре стереотипа и не находит класс без аннотации")
        void scannerFindsStereotypes() {
            try (var context = new AnnotationConfigApplicationContext(ScanConfig.class)) {
                assertThat(context.getBean(Stereotypes.PlainComponent.class).role())
                        .isEqualTo("component");
                assertThat(context.getBean(Stereotypes.BusinessService.class).role())
                        .isEqualTo("service");
                assertThat(context.getBean(Stereotypes.DataRepository.class).role())
                        .isEqualTo("repository");
                assertThat(context.getBean(Stereotypes.WebController.class).role())
                        .isEqualTo("controller");

                assertThatThrownBy(() -> context.getBean(Stereotypes.NotAComponent.class))
                        .isInstanceOf(org.springframework.beans.factory.NoSuchBeanDefinitionException.class);
            }
        }

        @Test
        @DisplayName("Все стереотипы сводятся к @Component — механика у них общая")
        void allStereotypesAreComponents() {
            assertThat(org.springframework.stereotype.Service.class
                    .isAnnotationPresent(org.springframework.stereotype.Component.class)).isTrue();
            assertThat(org.springframework.stereotype.Repository.class
                    .isAnnotationPresent(org.springframework.stereotype.Component.class)).isTrue();
            assertThat(org.springframework.stereotype.Controller.class
                    .isAnnotationPresent(org.springframework.stereotype.Component.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Слайд 143: @Configuration и proxyBeanMethods")
    class ConfigurationProxy {

        @BeforeEach
        void reset() {
            ProxyBeanMethods.reset();
        }

        @Test
        @DisplayName("Полный режим: вызов @Bean-метода возвращает бин из контейнера")
        void proxiedConfigurationReusesTheBean() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.ProxiedConfig.class)) {

                var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                assertThat(first.shared()).isSameAs(second.shared());
                assertThat(ProxyBeanMethods.INSTANCES.get()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Класс конфигурации сам обёрнут CGLIB-прокси")
        void configurationClassIsProxied() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.ProxiedConfig.class)) {

                Object config = context.getBean(ProxyBeanMethods.ProxiedConfig.class);

                assertThat(config.getClass()).isNotEqualTo(ProxyBeanMethods.ProxiedConfig.class);
                assertThat(config.getClass().getName()).contains("$$SpringCGLIB$$");
            }
        }

        @Test
        @DisplayName("Lite-режим: прокси нет, объект создаётся заново мимо контейнера")
        void liteConfigurationCreatesDuplicates() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.LiteConfig.class)) {

                var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                assertThat(first.shared()).isNotSameAs(second.shared());
                assertThat(ProxyBeanMethods.INSTANCES.get())
                        .as("бин из контейнера плюс две копии мимо него")
                        .isEqualTo(3);
            }
        }

        @Test
        @DisplayName("Lite-режим работает правильно, если зависимость передана параметром")
        void liteConfigurationDoneRight() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.LiteConfigDone.class)) {

                var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                assertThat(first.shared()).isSameAs(second.shared());
                assertThat(ProxyBeanMethods.INSTANCES.get()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("Слайд 147: @Transactional через AOP-прокси")
    class Transactions {

        @BeforeEach
        void reset() {
            TransactionalDemo.reset();
        }

        @Test
        @DisplayName("Аннотированный метод обрамляется begin/commit")
        void transactionIsOpenedAndCommitted() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class).save("ORD-1");

                assertThat(TransactionalDemo.LOG).containsExactly("begin", "save:ORD-1", "commit");
            }
        }

        @Test
        @DisplayName("Метод без аннотации транзакцию не открывает")
        void noAnnotationNoTransaction() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class)
                        .saveWithoutTransaction("ORD-2");

                assertThat(TransactionalDemo.LOG).containsExactly("save:ORD-2");
            }
        }

        @Test
        @DisplayName("Unchecked-исключение откатывает транзакцию")
        void uncheckedExceptionRollsBack() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                var service = context.getBean(TransactionalDemo.OrderService.class);

                assertThatThrownBy(service::failUnchecked).isInstanceOf(IllegalStateException.class);

                assertThat(TransactionalDemo.LOG).containsExactly("begin", "work", "rollback");
            }
        }

        @Test
        @DisplayName("Checked-исключение по умолчанию транзакцию КОММИТИТ — частый сюрприз")
        void checkedExceptionCommitsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                var service = context.getBean(TransactionalDemo.OrderService.class);

                assertThatThrownBy(service::failChecked).isInstanceOf(Exception.class);

                assertThat(TransactionalDemo.LOG).containsExactly("begin", "work", "commit");
            }
        }

        @Test
        @DisplayName("rollbackFor = Exception.class чинит это поведение")
        void rollbackForFixesIt() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                var service = context.getBean(TransactionalDemo.OrderService.class);

                assertThatThrownBy(service::failCheckedWithRollback).isInstanceOf(Exception.class);

                assertThat(TransactionalDemo.LOG).containsExactly("begin", "work", "rollback");
            }
        }

        @Test
        @DisplayName("Self-invocation транзакцию не открывает — прокси в стороне")
        void selfInvocationSkipsTheTransaction() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class).saveViaThis("ORD-3");

                assertThat(TransactionalDemo.LOG)
                        .containsExactly("save:ORD-3")
                        .doesNotContain("begin");
            }
        }
    }

    @Nested
    @DisplayName("Слайды 148–149: @ConditionalOnProperty и @ConditionalOnMissingBean")
    class Conditionals {

        @Test
        @DisplayName("@ConditionalOnProperty: без свойства бина нет")
        void propertyConditionIsOffByDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                assertThat(context.containsBean("metricsCollector")).isFalse();
            }
        }

        @Test
        @DisplayName("@ConditionalOnProperty: со свойством бин появляется")
        void propertyConditionTurnsOn() {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().getSystemProperties()
                        .put("sprbut.metrics.enabled", "true");
                context.register(ConditionalOnDemo.DefaultsConfig.class);
                context.refresh();

                assertThat(context.getBean("metricsCollector")).isEqualTo("метрики включены");
            } finally {
                System.clearProperty("sprbut.metrics.enabled");
            }
        }

        @Test
        @DisplayName("matchIfMissing = true — включено, пока явно не выключили")
        void matchIfMissingIsOnByDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                assertThat(context.getBean("auditCollector")).isEqualTo("аудит включён");
            }
        }

        @Test
        @DisplayName("@ConditionalOnMissingBean даёт значение по умолчанию")
        void missingBeanConditionProvidesDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                assertThat(context.getBean(ConditionalOnDemo.Notifier.class).send("привет"))
                        .isEqualTo("по умолчанию: привет");
            }
        }

        @Test
        @DisplayName("Пользовательский бин побеждает — вот как работает переопределение автоконфигурации")
        void userBeanWins() {
            try (var context = new AnnotationConfigApplicationContext()) {
                // порядок важен: пользовательская конфигурация обрабатывается раньше
                context.register(ConditionalOnDemo.UserConfig.class,
                        ConditionalOnDemo.DefaultsConfig.class);
                context.refresh();

                assertThat(context.getBean(ConditionalOnDemo.Notifier.class).send("привет"))
                        .isEqualTo("пользовательский: привет");
                assertThat(context.getBeanNamesForType(ConditionalOnDemo.Notifier.class))
                        .hasSize(1);
            }
        }
    }
}
