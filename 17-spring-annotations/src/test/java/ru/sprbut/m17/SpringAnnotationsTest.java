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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.PlainComponent.class).role(),
                    equalTo("component")
                );
                assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.BusinessService.class).role(),
                    equalTo("service")
                );
                assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.DataRepository.class).role(),
                    equalTo("repository")
                );
                assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.WebController.class).role(),
                    equalTo("controller")
                );

                assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, () -> context.getBean(Stereotypes.NotAComponent.class));
            }
        }

        @Test
        @DisplayName("Все стереотипы сводятся к @Component — механика у них общая")
        void allStereotypesAreComponents() {
            assertThat(
                "cannot verify that all stereotypes are components",
                org.springframework.stereotype.Service.class .isAnnotationPresent(org.springframework.stereotype.Component.class),
                equalTo(true)
            );
            assertThat(
                "cannot verify that all stereotypes are components",
                org.springframework.stereotype.Repository.class .isAnnotationPresent(org.springframework.stereotype.Component.class),
                equalTo(true)
            );
            assertThat(
                "cannot verify that all stereotypes are components",
                org.springframework.stereotype.Controller.class .isAnnotationPresent(org.springframework.stereotype.Component.class),
                equalTo(true)
            );
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

                assertThat(
                    "cannot verify that proxied configuration reuses the bean",
                    first.shared(),
                    sameInstance(second.shared())
                );
                assertThat(
                    "cannot verify that proxied configuration reuses the bean",
                    ProxyBeanMethods.INSTANCES.get(),
                    equalTo(1)
                );
            }
        }

        @Test
        @DisplayName("Класс конфигурации сам обёрнут CGLIB-прокси")
        void configurationClassIsProxied() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.ProxiedConfig.class)) {

                Object config = context.getBean(ProxyBeanMethods.ProxiedConfig.class);

                assertThat(
                    "configuration class cannot be wrapped in a CGLIB proxy",
                    config.getClass().getName(),
                    containsString("$$SpringCGLIB$$")
                );
            }
        }

        @Test
        @DisplayName("Lite-режим: прокси нет, объект создаётся заново мимо контейнера")
        void liteConfigurationCreatesDuplicates() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.LiteConfig.class)) {

                var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                assertThat(
                    "cannot verify that lite configuration creates duplicates",
                    first.shared(),
                    not(sameInstance(second.shared()))
                );
                assertThat(
                    "lite configuration cannot create a copy per call",
                    ProxyBeanMethods.INSTANCES.get(),
                    equalTo(3)
                );
            }
        }

        @Test
        @DisplayName("Lite-режим работает правильно, если зависимость передана параметром")
        void liteConfigurationDoneRight() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.LiteConfigDone.class)) {

                var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                assertThat(
                    "cannot verify that lite configuration done right",
                    first.shared(),
                    sameInstance(second.shared())
                );
                assertThat(
                    "cannot verify that lite configuration done right",
                    ProxyBeanMethods.INSTANCES.get(),
                    equalTo(1)
                );
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

                assertThat(
                    "cannot verify that transaction is opened and committed",
                    TransactionalDemo.LOG,
                    contains("begin", "save:ORD-1", "commit")
                );
            }
        }

        @Test
        @DisplayName("Метод без аннотации транзакцию не открывает")
        void noAnnotationNoTransaction() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class)
                        .saveWithoutTransaction("ORD-2");

                assertThat(
                    "cannot verify that no annotation no transaction",
                    TransactionalDemo.LOG,
                    contains("save:ORD-2")
                );
            }
        }

        @Test
        @DisplayName("Unchecked-исключение откатывает транзакцию")
        void uncheckedExceptionRollsBack() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                var service = context.getBean(TransactionalDemo.OrderService.class);

                assertThrows(IllegalStateException.class, service::failUnchecked);

                assertThat(
                    "cannot verify that unchecked exception rolls back",
                    TransactionalDemo.LOG,
                    contains("begin", "work", "rollback")
                );
            }
        }

        @Test
        @DisplayName("Checked-исключение по умолчанию транзакцию КОММИТИТ — частый сюрприз")
        void checkedExceptionCommitsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                var service = context.getBean(TransactionalDemo.OrderService.class);

                assertThrows(Exception.class, service::failChecked);

                assertThat(
                    "cannot verify that checked exception commits by default",
                    TransactionalDemo.LOG,
                    contains("begin", "work", "commit")
                );
            }
        }

        @Test
        @DisplayName("rollbackFor = Exception.class чинит это поведение")
        void rollbackForFixesIt() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                var service = context.getBean(TransactionalDemo.OrderService.class);

                assertThrows(Exception.class, service::failCheckedWithRollback);

                assertThat(
                    "cannot verify that rollback for fixes it",
                    TransactionalDemo.LOG,
                    contains("begin", "work", "rollback")
                );
            }
        }

        @Test
        @DisplayName("Self-invocation транзакцию не открывает — прокси в стороне")
        void selfInvocationSkipsTheTransaction() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class).saveViaThis("ORD-3");

                assertThat(
                    "self invocation cannot bypass the transactional proxy",
                    TransactionalDemo.LOG,
                    contains("save:ORD-3")
                );
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
                assertThat(
                    "cannot verify that property condition is off by default",
                    context.containsBean("metricsCollector"),
                    equalTo(false)
                );
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

                assertThat(
                    "cannot verify that property condition turns on",
                    context.getBean("metricsCollector"),
                    equalTo("метрики включены")
                );
            } finally {
                System.clearProperty("sprbut.metrics.enabled");
            }
        }

        @Test
        @DisplayName("matchIfMissing = true — включено, пока явно не выключили")
        void matchIfMissingIsOnByDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                assertThat(
                    "cannot verify that match if missing is on by default",
                    context.getBean("auditCollector"),
                    equalTo("аудит включён")
                );
            }
        }

        @Test
        @DisplayName("@ConditionalOnMissingBean даёт значение по умолчанию")
        void missingBeanConditionProvidesDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                assertThat(
                    "cannot verify that missing bean condition provides default",
                    context.getBean(ConditionalOnDemo.Notifier.class).send("привет"),
                    equalTo("по умолчанию: привет")
                );
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

                assertThat(
                    "cannot verify that user bean wins",
                    context.getBean(ConditionalOnDemo.Notifier.class).send("привет"),
                    equalTo("пользовательский: привет")
                );
                assertThat(
                    "user bean cannot remain the only candidate",
                    context.getBeanNamesForType(ConditionalOnDemo.Notifier.class).length,
                    equalTo(1)
                );
            }
        }
    }
}
