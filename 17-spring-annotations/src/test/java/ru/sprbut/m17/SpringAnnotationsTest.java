/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m17;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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

/**
 * Слайды 139–149: аннотации Spring.
 * @since 1.0
 */
@DisplayName("Слайды 139–149: аннотации Spring")
class SpringAnnotationsTest {

    @Nested
/**
 * Слайды 140–144: стереотипы.
 * @since 1.0
 */
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
                MatcherAssert.assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.PlainComponent.class).role(),
                    Matchers.equalTo("component")
                );
                MatcherAssert.assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.BusinessService.class).role(),
                    Matchers.equalTo("service")
                );
                MatcherAssert.assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.DataRepository.class).role(),
                    Matchers.equalTo("repository")
                );
                MatcherAssert.assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.WebController.class).role(),
                    Matchers.equalTo("controller")
                );

                Assertions.assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, () -> context.getBean(Stereotypes.NotAComponent.class));
            }
        }

        @Test
        @DisplayName("Все стереотипы сводятся к @Component — механика у них общая")
        void allStereotypesAreComponents() {
            MatcherAssert.assertThat(
                "cannot verify that all stereotypes are components",
                org.springframework.stereotype.Service.class .isAnnotationPresent(org.springframework.stereotype.Component.class),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that all stereotypes are components",
                org.springframework.stereotype.Repository.class .isAnnotationPresent(org.springframework.stereotype.Component.class),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that all stereotypes are components",
                org.springframework.stereotype.Controller.class .isAnnotationPresent(org.springframework.stereotype.Component.class),
                Matchers.equalTo(true)
            );
        }
    }

    @Nested
/**
 * Слайд 143: @Configuration и proxyBeanMethods.
 * @since 1.0
 */
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

                final var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                final var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                MatcherAssert.assertThat(
                    "cannot verify that proxied configuration reuses the bean",
                    first.shared(),
                    Matchers.sameInstance(second.shared())
                );
                MatcherAssert.assertThat(
                    "cannot verify that proxied configuration reuses the bean",
                    ProxyBeanMethods.INSTANCES.get(),
                    Matchers.equalTo(1)
                );
            }
        }

        @Test
        @DisplayName("Класс конфигурации сам обёрнут CGLIB-прокси")
        void configurationClassIsProxied() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.ProxiedConfig.class)) {

                final Object config = context.getBean(ProxyBeanMethods.ProxiedConfig.class);

                MatcherAssert.assertThat(
                    "configuration class cannot be wrapped in a CGLIB proxy",
                    config.getClass().getName(),
                    Matchers.containsString("$$SpringCGLIB$$")
                );
            }
        }

        @Test
        @DisplayName("Lite-режим: прокси нет, объект создаётся заново мимо контейнера")
        void liteConfigurationCreatesDuplicates() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.LiteConfig.class)) {

                final var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                final var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                MatcherAssert.assertThat(
                    "cannot verify that lite configuration creates duplicates",
                    first.shared(),
                    Matchers.not(Matchers.sameInstance(second.shared()))
                );
                MatcherAssert.assertThat(
                    "lite configuration cannot create a copy per call",
                    ProxyBeanMethods.INSTANCES.get(),
                    Matchers.equalTo(3)
                );
            }
        }

        @Test
        @DisplayName("Lite-режим работает правильно, если зависимость передана параметром")
        void liteConfigurationDoneRight() {
            try (var context = new AnnotationConfigApplicationContext(
                    ProxyBeanMethods.LiteConfigDone.class)) {

                final var first = context.getBean("first", ProxyBeanMethods.Consumer.class);
                final var second = context.getBean("second", ProxyBeanMethods.Consumer.class);

                MatcherAssert.assertThat(
                    "cannot verify that lite configuration done right",
                    first.shared(),
                    Matchers.sameInstance(second.shared())
                );
                MatcherAssert.assertThat(
                    "cannot verify that lite configuration done right",
                    ProxyBeanMethods.INSTANCES.get(),
                    Matchers.equalTo(1)
                );
            }
        }
    }

    @Nested
/**
 * Слайд 147: @Transactional через AOP-прокси.
 * @since 1.0
 */
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

                MatcherAssert.assertThat(
                    "cannot verify that transaction is opened and committed",
                    TransactionalDemo.LOG,
                    Matchers.contains("begin", "save:ORD-1", "commit")
                );
            }
        }

        @Test
        @DisplayName("Метод без аннотации транзакцию не открывает")
        void noAnnotationNoTransaction() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class)
                        .saveWithoutTransaction("ORD-2");

                MatcherAssert.assertThat(
                    "cannot verify that no annotation no transaction",
                    TransactionalDemo.LOG,
                    Matchers.contains("save:ORD-2")
                );
            }
        }

        @Test
        @DisplayName("Unchecked-исключение откатывает транзакцию")
        void uncheckedExceptionRollsBack() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                final var service = context.getBean(TransactionalDemo.OrderService.class);

                Assertions.assertThrows(IllegalStateException.class, service::failUnchecked);

                MatcherAssert.assertThat(
                    "cannot verify that unchecked exception rolls back",
                    TransactionalDemo.LOG,
                    Matchers.contains("begin", "work", "rollback")
                );
            }
        }

        @Test
        @DisplayName("Checked-исключение по умолчанию транзакцию КОММИТИТ — частый сюрприз")
        void checkedExceptionCommitsByDefault() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                final var service = context.getBean(TransactionalDemo.OrderService.class);

                Assertions.assertThrows(Exception.class, service::failChecked);

                MatcherAssert.assertThat(
                    "cannot verify that checked exception commits by default",
                    TransactionalDemo.LOG,
                    Matchers.contains("begin", "work", "commit")
                );
            }
        }

        @Test
        @DisplayName("rollbackFor = Exception.class чинит это поведение")
        void rollbackForFixesIt() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                final var service = context.getBean(TransactionalDemo.OrderService.class);

                Assertions.assertThrows(Exception.class, service::failCheckedWithRollback);

                MatcherAssert.assertThat(
                    "cannot verify that rollback for fixes it",
                    TransactionalDemo.LOG,
                    Matchers.contains("begin", "work", "rollback")
                );
            }
        }

        @Test
        @DisplayName("Self-invocation транзакцию не открывает — прокси в стороне")
        void selfInvocationSkipsTheTransaction() {
            try (var context = new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)) {
                context.getBean(TransactionalDemo.OrderService.class).saveViaThis("ORD-3");

                MatcherAssert.assertThat(
                    "self invocation cannot bypass the transactional proxy",
                    TransactionalDemo.LOG,
                    Matchers.contains("save:ORD-3")
                );
            }
        }
    }

    @Nested
/**
 * Слайды 148–149: @ConditionalOnProperty и @ConditionalOnMissingBean.
 * @since 1.0
 */
    @DisplayName("Слайды 148–149: @ConditionalOnProperty и @ConditionalOnMissingBean")
    class Conditionals {

        @Test
        @DisplayName("@ConditionalOnProperty: без свойства бина нет")
        void propertyConditionIsOffByDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that property condition is off by default",
                    context.containsBean("metricsCollector"),
                    Matchers.equalTo(false)
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

                MatcherAssert.assertThat(
                    "cannot verify that property condition turns on",
                    context.getBean("metricsCollector"),
                    Matchers.equalTo("метрики включены")
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
                MatcherAssert.assertThat(
                    "cannot verify that match if missing is on by default",
                    context.getBean("auditCollector"),
                    Matchers.equalTo("аудит включён")
                );
            }
        }

        @Test
        @DisplayName("@ConditionalOnMissingBean даёт значение по умолчанию")
        void missingBeanConditionProvidesDefault() {
            try (var context = new AnnotationConfigApplicationContext(
                    ConditionalOnDemo.DefaultsConfig.class)) {
                MatcherAssert.assertThat(
                    "cannot verify that missing bean condition provides default",
                    context.getBean(ConditionalOnDemo.Notifier.class).send("привет"),
                    Matchers.equalTo("по умолчанию: привет")
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

                MatcherAssert.assertThat(
                    "cannot verify that user bean wins",
                    context.getBean(ConditionalOnDemo.Notifier.class).send("привет"),
                    Matchers.equalTo("пользовательский: привет")
                );
                MatcherAssert.assertThat(
                    "user bean cannot remain the only candidate",
                    context.getBeanNamesForType(ConditionalOnDemo.Notifier.class).length,
                    Matchers.equalTo(1)
                );
            }
        }
    }
}
