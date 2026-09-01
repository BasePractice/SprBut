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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.sprbut.m17.conditionals.ConditionalOnDemo;
import ru.sprbut.m17.configuration.ProxyBeanMethods;
import ru.sprbut.m17.stereotypes.Stereotypes;
import ru.sprbut.m17.transactional.TransactionalDemo;

/**
 * Слайды 139–149: аннотации Spring.
 * @since 1.0
 */
@DisplayName("Слайды 139–149: аннотации Spring")
final class SpringAnnotationsTest {

    /**
     * Слайды 140–144: стереотипы.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайды 140–144: стереотипы")
    final class StereotypeScanning {

        @Test
        @DisplayName("Сканер находит класс с @Component")
        void scannerFindsComponent() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScanConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that scanner finds stereotypes",
                    context.getBean(Stereotypes.PlainComponent.class).role(),
                    Matchers.equalTo("component")
                );
            }
        }

        @Test
        @DisplayName("Сканер находит класс с @Service")
        void scannerFindsService() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScanConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "scanner cannot find the service",
                    context.getBean(Stereotypes.BusinessService.class).role(),
                    Matchers.equalTo("service")
                );
            }
        }

        @Test
        @DisplayName("Сканер находит класс с @Repository")
        void scannerFindsRepository() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScanConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "scanner cannot find the repository",
                    context.getBean(Stereotypes.DataRepository.class).role(),
                    Matchers.equalTo("repository")
                );
            }
        }

        @Test
        @DisplayName("Сканер находит класс с @Controller")
        void scannerFindsController() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScanConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "scanner cannot find the controller",
                    context.getBean(Stereotypes.WebController.class).role(),
                    Matchers.equalTo("controller")
                );
            }
        }

        @Test
        @DisplayName("Класс без стереотипа сканером не найден")
        void scannerSkipsPlainClass() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ScanConfig.class)
            ) {
                Assertions.assertThrows(
                    NoSuchBeanDefinitionException.class,
                    () -> context.getBean(Stereotypes.NotAComponent.class)
                );
            }
        }

        @Test
        @DisplayName("@Service сводится к @Component")
        void serviceIsAComponent() {
            MatcherAssert.assertThat(
                "service cannot reduce to a component",
                Service.class.isAnnotationPresent(Component.class),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("@Repository сводится к @Component")
        void repositoryIsAComponent() {
            MatcherAssert.assertThat(
                "repository cannot reduce to a component",
                Repository.class.isAnnotationPresent(Component.class),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("@Controller сводится к @Component")
        void controllerIsAComponent() {
            MatcherAssert.assertThat(
                "controller cannot reduce to a component",
                Controller.class.isAnnotationPresent(Component.class),
                Matchers.equalTo(true)
            );
        }

        @Configuration
        @ComponentScan(basePackageClasses = Stereotypes.class)
        static final class ScanConfig {
        }
    }

    /**
     * Слайд 143: @Configuration и proxyBeanMethods.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 143: @Configuration и proxyBeanMethods")
    final class ConfigurationProxy {

        @BeforeEach
        void reset() {
            ProxyBeanMethods.reset();
        }

        @Test
        @DisplayName("Полный режим: вызов @Bean-метода возвращает бин из контейнера")
        void proxiedConfigurationReusesTheBean() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ProxyBeanMethods.ProxiedConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that proxied configuration reuses the bean",
                    context.getBean("first", ProxyBeanMethods.Consumer.class).shared(),
                    Matchers.sameInstance(
                        context.getBean("second", ProxyBeanMethods.Consumer.class).shared()
                    )
                );
            }
        }

        @Test
        @DisplayName("Класс конфигурации сам обёрнут CGLIB-прокси")
        void configurationClassIsProxied() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ProxyBeanMethods.ProxiedConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "configuration class cannot be wrapped in a CGLIB proxy",
                    context.getBean(ProxyBeanMethods.ProxiedConfig.class).getClass().getName(),
                    Matchers.containsString("$$SpringCGLIB$$")
                );
            }
        }

        @Test
        @DisplayName("Lite-режим: прокси нет, объект создаётся заново мимо контейнера")
        void liteConfigurationCreatesDuplicates() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ProxyBeanMethods.LiteConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that lite configuration creates duplicates",
                    context.getBean("first", ProxyBeanMethods.Consumer.class).shared(),
                    Matchers.not(
                        Matchers.sameInstance(
                            context.getBean("second", ProxyBeanMethods.Consumer.class).shared()
                        )
                    )
                );
            }
        }

        @Test
        @DisplayName("Lite-режим работает правильно, если зависимость передана параметром")
        void liteConfigurationDoneRight() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ProxyBeanMethods.LiteConfigDone.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that lite configuration done right",
                    context.getBean("first", ProxyBeanMethods.Consumer.class).shared(),
                    Matchers.sameInstance(
                        context.getBean("second", ProxyBeanMethods.Consumer.class).shared()
                    )
                );
            }
        }
    }

    /**
     * Слайд 147: @Transactional через AOP-прокси.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 147: @Transactional через AOP-прокси")
    final class Transactions {

        @BeforeEach
        void reset() {
            TransactionalDemo.reset();
        }

        @Test
        @DisplayName("Аннотированный метод обрамляется begin/commit")
        void transactionIsOpenedAndCommitted() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)
            ) {
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)
            ) {
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)
            ) {
                Assertions.assertThrows(
                    IllegalStateException.class,
                    context.getBean(TransactionalDemo.OrderService.class)::failUnchecked
                );
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)
            ) {
                Assertions.assertThrows(
                    Exception.class,
                    context.getBean(TransactionalDemo.OrderService.class)::failChecked
                );
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)
            ) {
                Assertions.assertThrows(
                    Exception.class,
                    context.getBean(TransactionalDemo.OrderService.class)::failCheckedWithRollback
                );
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(TransactionalDemo.Config.class)
            ) {
                context.getBean(TransactionalDemo.OrderService.class).saveViaThis("ORD-3");
                MatcherAssert.assertThat(
                    "self invocation cannot bypass the transactional proxy",
                    TransactionalDemo.LOG,
                    Matchers.contains("save:ORD-3")
                );
            }
        }
    }

    /**
     * Слайды 148–149: @ConditionalOnProperty и @ConditionalOnMissingBean.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайды 148–149: @ConditionalOnProperty и @ConditionalOnMissingBean")
    final class Conditionals {

        @Test
        @DisplayName("@ConditionalOnProperty: без свойства бина нет")
        void propertyConditionIsOffByDefault() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalOnDemo.DefaultsConfig.class)
            ) {
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()
            ) {
                context.getEnvironment()
                    .getSystemProperties()
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalOnDemo.DefaultsConfig.class)
            ) {
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
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(ConditionalOnDemo.DefaultsConfig.class)
            ) {
                MatcherAssert.assertThat(
                    "cannot verify that missing bean condition provides default",
                    context.getBean(ConditionalOnDemo.Notifier.class).send("привет"),
                    Matchers.equalTo("по умолчанию: привет")
                );
            }
        }

        @Test
        @DisplayName("Пользовательский бин побеждает — так работает переопределение")
        void userBeanWins() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()
            ) {
                context.register(
                    ConditionalOnDemo.UserConfig.class, ConditionalOnDemo.DefaultsConfig.class
                );
                context.refresh();
                MatcherAssert.assertThat(
                    "cannot verify that user bean wins",
                    context.getBean(ConditionalOnDemo.Notifier.class).send("привет"),
                    Matchers.equalTo("пользовательский: привет")
                );
            }
        }

        @Test
        @DisplayName("бин по умолчанию при этом вообще не создаётся")
        void defaultBeanIsNotRegistered() {
            try (
                AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext()
            ) {
                context.register(
                    ConditionalOnDemo.UserConfig.class, ConditionalOnDemo.DefaultsConfig.class
                );
                context.refresh();
                MatcherAssert.assertThat(
                    "user bean cannot remain the only candidate",
                    context.getBeanNamesForType(ConditionalOnDemo.Notifier.class).length,
                    Matchers.equalTo(1)
                );
            }
        }
    }
}
