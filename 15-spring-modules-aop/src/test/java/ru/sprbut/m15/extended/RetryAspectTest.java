/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m15.extended;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Расширенный пример: своя @Retryable через AOP и обход self-invocation.
 * @since 1.0
 */
@DisplayName("Расширенный пример: своя @Retryable через AOP и обход self-invocation")
final class RetryAspectTest {

    /**
     * Контекст один на класс: все сценарии смотрят на один и тот же набор
     * прокси, поднимать его заново на каждый тест значит сравнивать разные
     * прокси.
     * @checkstyle ProhibitFieldsInTestClassesCheck (21 lines)
     */
    private AnnotationConfigApplicationContext context;

    /**
     * Сервис платежей, взятый из контекста прокси.
     */
    private PaymentService payments;

    /**
     * Отдельный бин-исполнитель — правильный обход self-invocation.
     */
    private ChargeExecutor executor;

    /**
     * Аспект, чей журнал проверяют все сценарии.
     */
    private RetryAspect aspect;

    @BeforeEach
    void setUp() {
        this.context = new AnnotationConfigApplicationContext(ExtendedAopConfig.class);
        this.payments = this.context.getBean(PaymentService.class);
        this.executor = this.context.getBean(ChargeExecutor.class);
        this.aspect = this.context.getBean(RetryAspect.class);
        this.payments.reset();
        this.executor.reset();
        this.aspect.clear();
    }

    @AfterEach
    void tearDown() {
        this.context.close();
    }

    /**
     * Аннотация работает как @Transactional: метаданные + аспект.
     * @since 1.0
     */
    @Nested
    @DisplayName("Аннотация работает как @Transactional: метаданные + аспект")
    final class Behaviour {

        @Test
        @DisplayName("Успешный вызов проходит с первой попытки")
        void succeedsImmediately() {
            MatcherAssert.assertThat(
                "cannot verify that succeeds immediately",
                RetryAspectTest.this.payments.charge("ORD-1"),
                Matchers.equalTo("оплачен ORD-1")
            );
        }

        @Test
        @DisplayName("и аспект отмечает единственную попытку")
        void logsASingleAttempt() {
            RetryAspectTest.this.payments.charge("ORD-1");
            MatcherAssert.assertThat(
                "aspect cannot log a single attempt",
                RetryAspectTest.this.aspect.log(),
                Matchers.contains("success:charge:попытка1")
            );
        }

        @Test
        @DisplayName("Аспект повторяет вызов до успеха")
        void retriesUntilSuccess() {
            RetryAspectTest.this.payments.failFirst(2);
            MatcherAssert.assertThat(
                "cannot verify that retries until success",
                RetryAspectTest.this.payments.charge("ORD-2"),
                Matchers.equalTo("оплачен ORD-2")
            );
        }

        @Test
        @DisplayName("цель вызывается ровно столько раз, сколько было попыток")
        void callsTheTargetOncePerAttempt() {
            RetryAspectTest.this.payments.failFirst(2);
            RetryAspectTest.this.payments.charge("ORD-2");
            MatcherAssert.assertThat(
                "target cannot be called once per attempt",
                RetryAspectTest.this.payments.executions(),
                Matchers.equalTo(3)
            );
        }

        @Test
        @DisplayName("журнал аспекта показывает две неудачи и успех")
        void logsEveryAttempt() {
            RetryAspectTest.this.payments.failFirst(2);
            RetryAspectTest.this.payments.charge("ORD-2");
            MatcherAssert.assertThat(
                "aspect cannot log every attempt",
                RetryAspectTest.this.aspect.log(),
                Matchers.contains(
                    "fail:charge:попытка1", "fail:charge:попытка2", "success:charge:попытка3"
                )
            );
        }

        @Test
        @DisplayName("Исчерпав попытки, аспект пробрасывает последнее исключение")
        void rethrowsAfterExhaustion() {
            RetryAspectTest.this.payments.failFirst(99);
            MatcherAssert.assertThat(
                "cannot verify that rethrows after exhaustion",
                Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> RetryAspectTest.this.payments.charge("ORD-3")
                ).getMessage(),
                Matchers.containsString("сбой платежа №3")
            );
        }

        @Test
        @DisplayName("и отмечает исчерпание попыток в журнале")
        void logsTheExhaustion() {
            RetryAspectTest.this.payments.failFirst(99);
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> RetryAspectTest.this.payments.charge("ORD-3")
            );
            MatcherAssert.assertThat(
                "aspect cannot log the exhaustion",
                RetryAspectTest.this.aspect.log(),
                Matchers.hasItems("exhausted:charge")
            );
        }

        @Test
        @DisplayName("попыток ровно столько, сколько объявлено в аннотации")
        void stopsAtTheDeclaredAttempts() {
            RetryAspectTest.this.payments.failFirst(99);
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> RetryAspectTest.this.payments.charge("ORD-3")
            );
            MatcherAssert.assertThat(
                "aspect cannot stop at the declared attempts",
                RetryAspectTest.this.payments.executions(),
                Matchers.equalTo(3)
            );
        }

        @Test
        @DisplayName("Pointcut по аннотации не зависит от структуры пакетов")
        void pointcutIsAnnotationDriven() {
            RetryAspectTest.this.payments.failFirst(1);
            RetryAspectTest.this.executor.failFirst(1);
            RetryAspectTest.this.payments.charge("ORD-4");
            RetryAspectTest.this.executor.execute("ORD-5");
            MatcherAssert.assertThat(
                "cannot verify that pointcut is annotation driven",
                RetryAspectTest.this.aspect.attemptsOf("charge"),
                Matchers.equalTo(2L)
            );
        }

        @Test
        @DisplayName("и срабатывает на втором помеченном методе тоже")
        void pointcutCoversTheSecondMethod() {
            RetryAspectTest.this.executor.failFirst(1);
            RetryAspectTest.this.executor.execute("ORD-5");
            MatcherAssert.assertThat(
                "pointcut cannot cover the second annotated method",
                RetryAspectTest.this.aspect.attemptsOf("execute"),
                Matchers.equalTo(2L)
            );
        }
    }

    /**
     * Слайд 124: self-invocation и три способа его обойти.
     * @since 1.0
     */
    @Nested
    @DisplayName("Слайд 124: self-invocation и три способа его обойти")
    final class SelfInvocation {

        @Test
        @DisplayName("Вызов через this минует прокси — ретрая нет, ошибка вылетает сразу")
        void thisCallBypassesTheProxy() {
            RetryAspectTest.this.payments.failFirst(1);
            MatcherAssert.assertThat(
                "cannot verify that this call bypasses the proxy",
                Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> RetryAspectTest.this.payments.chargeViaThis("ORD-6")
                ).getMessage(),
                Matchers.containsString("сбой платежа №1")
            );
        }

        @Test
        @DisplayName("цель при этом вызвана ровно один раз")
        void thisCallRunsTheTargetOnce() {
            RetryAspectTest.this.payments.failFirst(1);
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> RetryAspectTest.this.payments.chargeViaThis("ORD-6")
            );
            MatcherAssert.assertThat(
                "target cannot run exactly once on a this call",
                RetryAspectTest.this.payments.executions(),
                Matchers.equalTo(1)
            );
        }

        @Test
        @DisplayName("аспект такого вызова вообще не видит")
        void thisCallStaysInvisibleToTheAspect() {
            RetryAspectTest.this.payments.failFirst(1);
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> RetryAspectTest.this.payments.chargeViaThis("ORD-6")
            );
            MatcherAssert.assertThat(
                "this call cannot stay invisible to the aspect",
                RetryAspectTest.this.aspect.log()
                    .stream()
                    .anyMatch(entry -> entry.contains(":charge:")),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("Обход 1: самовнедрение через ObjectProvider — ретрай работает")
        void selfInjectionRestoresTheAspect() {
            RetryAspectTest.this.payments.failFirst(1);
            MatcherAssert.assertThat(
                "cannot verify that self injection restores the aspect",
                RetryAspectTest.this.payments.chargeViaSelf("ORD-7"),
                Matchers.equalTo("оплачен ORD-7")
            );
        }

        @Test
        @DisplayName("при самовнедрении цель вызывается дважды")
        void selfInjectionRunsTheTargetTwice() {
            RetryAspectTest.this.payments.failFirst(1);
            RetryAspectTest.this.payments.chargeViaSelf("ORD-7");
            MatcherAssert.assertThat(
                "target cannot run twice on a self injected call",
                RetryAspectTest.this.payments.executions(),
                Matchers.equalTo(2)
            );
        }

        @Test
        @DisplayName("и аспект насчитывает те же две попытки")
        void selfInjectionIsVisibleToTheAspect() {
            RetryAspectTest.this.payments.failFirst(1);
            RetryAspectTest.this.payments.chargeViaSelf("ORD-7");
            MatcherAssert.assertThat(
                "self injected call cannot be visible to the aspect",
                RetryAspectTest.this.aspect.attemptsOf("charge"),
                Matchers.equalTo(2L)
            );
        }

        @Test
        @DisplayName("Обход 2: AopContext.currentProxy — работает, но требует exposeProxy")
        void aopContextRestoresTheAspect() {
            RetryAspectTest.this.payments.failFirst(1);
            MatcherAssert.assertThat(
                "cannot verify that aop context restores the aspect",
                RetryAspectTest.this.payments.chargeViaAopContext("ORD-8"),
                Matchers.equalTo("оплачен ORD-8")
            );
        }

        @Test
        @DisplayName("и аспект насчитывает две попытки")
        void aopContextCallIsVisibleToTheAspect() {
            RetryAspectTest.this.payments.failFirst(1);
            RetryAspectTest.this.payments.chargeViaAopContext("ORD-8");
            MatcherAssert.assertThat(
                "aop context call cannot be visible to the aspect",
                RetryAspectTest.this.aspect.attemptsOf("charge"),
                Matchers.equalTo(2L)
            );
        }

        @Test
        @DisplayName("Правильное решение: вынести метод в отдельный бин")
        void separateBeanIsTheCleanSolution() {
            RetryAspectTest.this.executor.failFirst(2);
            MatcherAssert.assertThat(
                "cannot verify that separate bean is the clean solution",
                RetryAspectTest.this.payments.chargeViaSeparateBean("ORD-9"),
                Matchers.equalTo("оплачен ORD-9")
            );
        }

        @Test
        @DisplayName("отдельный бин вызывается столько раз, сколько было попыток")
        void separateBeanRunsOncePerAttempt() {
            RetryAspectTest.this.executor.failFirst(2);
            RetryAspectTest.this.payments.chargeViaSeparateBean("ORD-9");
            MatcherAssert.assertThat(
                "separate bean cannot run once per attempt",
                RetryAspectTest.this.executor.executions(),
                Matchers.equalTo(3)
            );
        }

        @Test
        @DisplayName("и аспект насчитывает три попытки")
        void separateBeanIsVisibleToTheAspect() {
            RetryAspectTest.this.executor.failFirst(2);
            RetryAspectTest.this.payments.chargeViaSeparateBean("ORD-9");
            MatcherAssert.assertThat(
                "separate bean cannot be visible to the aspect",
                RetryAspectTest.this.aspect.attemptsOf("execute"),
                Matchers.equalTo(3L)
            );
        }

        @Test
        @DisplayName("Без exposeProxy обход через AopContext падает — флаг не бесплатный")
        void aopContextNeedsExposeProxy() {
            try (
                AnnotationConfigApplicationContext plain =
                    new AnnotationConfigApplicationContext(PlainConfig.class)
            ) {
                final PaymentService service = plain.getBean(PaymentService.class);
                MatcherAssert.assertThat(
                    "cannot verify that aop context needs expose proxy",
                    Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> service.chargeViaAopContext("ORD-10")
                    ).getMessage(),
                    Matchers.containsString("Cannot find current proxy")
                );
            }
        }

        /**
         * Та же конфигурация, но без {@code exposeProxy} и без сканирования пакета.
         * @since 1.0
         */
        // @Configuration нельзя объявлять final: Spring оборачивает такой класс
        // CGLIB-подклассом и падает на попытке унаследоваться
        @Configuration
        @EnableAspectJAutoProxy
        @Import({PaymentService.class, ChargeExecutor.class, RetryAspect.class})
        @SuppressWarnings("PMD.JUnitTestClassShouldBeFinal")
        static class PlainConfig {
        }
    }

    /**
     * Прокси — это другой объект, а не ваш бин.
     * @since 1.0
     */
    @Nested
    @DisplayName("Прокси — это другой объект, а не ваш бин")
    final class ProxyIsNotTheTarget {

        @Test
        @DisplayName("Из контекста приходит CGLIB-подкласс, а не сам PaymentService")
        void contextReturnsAProxy() {
            MatcherAssert.assertThat(
                "cannot verify that context returns a proxy",
                AopUtils.isCglibProxy(RetryAspectTest.this.payments),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("класс прокси не совпадает с классом бина")
        void proxyClassDiffersFromTheTarget() {
            MatcherAssert.assertThat(
                "proxy class cannot differ from the target class",
                RetryAspectTest.this.payments.getClass(),
                Matchers.not(Matchers.equalTo(PaymentService.class))
            );
        }

        @Test
        @DisplayName("но наследуется от него")
        void proxyExtendsTheTarget() {
            MatcherAssert.assertThat(
                "proxy cannot extend the target class",
                RetryAspectTest.this.payments.getClass().getSuperclass(),
                Matchers.equalTo(PaymentService.class)
            );
        }

        @Test
        @DisplayName("Поэтому состояние читается только методами: они делегируются цели")
        void stateIsReachableOnlyThroughMethods() {
            RetryAspectTest.this.payments.failFirst(1);
            RetryAspectTest.this.payments.charge("ORD-11");
            MatcherAssert.assertThat(
                "cannot verify that state is reachable only through methods",
                RetryAspectTest.this.payments.executions(),
                Matchers.equalTo(2)
            );
        }
    }
}
