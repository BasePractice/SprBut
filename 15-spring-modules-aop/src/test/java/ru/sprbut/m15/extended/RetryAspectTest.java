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
     * Контекст.
     */
    private AnnotationConfigApplicationContext context;
    /**
     * Значение {@code payments}.
     */
    private PaymentService payments;
    /**
     * Исполнитель.
     */
    private ChargeExecutor executor;
    /**
     * Значение {@code aspect}.
     * @since 1.0
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

    @Nested
/**
 * Аннотация работает как @Transactional: метаданные + аспект.
 * @since 1.0
 */
    @DisplayName("Аннотация работает как @Transactional: метаданные + аспект")
    final class Behaviour {

        @Test
        @DisplayName("Успешный вызов проходит с первой попытки")
        void succeedsImmediately() {
            MatcherAssert.assertThat(
                "cannot verify that succeeds immediately",
                payments.charge("ORD-1"),
                Matchers.equalTo("оплачен ORD-1")
            );
            MatcherAssert.assertThat(
                "cannot verify that succeeds immediately",
                aspect.log(),
                Matchers.contains("success:charge:попытка1")
            );
        }

        @Test
        @DisplayName("Аспект повторяет вызов до успеха")
        void retriesUntilSuccess() {
            payments.failFirst(2);
            MatcherAssert.assertThat(
                "cannot verify that retries until success",
                payments.charge("ORD-2"),
                Matchers.equalTo("оплачен ORD-2")
            );
            MatcherAssert.assertThat(
                "cannot verify that retries until success",
                payments.executions(),
                Matchers.equalTo(3)
            );
            MatcherAssert.assertThat(
                "cannot verify that retries until success",
                aspect.log(),
                Matchers.contains("fail:charge:попытка1", "fail:charge:попытка2", "success:charge:попытка3")
            );
        }

        @Test
        @DisplayName("Исчерпав попытки, аспект пробрасывает последнее исключение")
        void rethrowsAfterExhaustion() {
            payments.failFirst(99);
            MatcherAssert.assertThat(
                "cannot verify that rethrows after exhaustion",
                Assertions.assertThrows(IllegalStateException.class, () -> payments.charge("ORD-3")).getMessage(),
                Matchers.containsString("сбой платежа №3")
            );
            MatcherAssert.assertThat(
                "cannot verify that rethrows after exhaustion",
                aspect.log(),
                Matchers.hasItems("exhausted:charge")
            );
            MatcherAssert.assertThat(
                "cannot verify that rethrows after exhaustion",
                payments.executions(),
                Matchers.equalTo(3)
            );
        }

        @Test
        @DisplayName("Pointcut по аннотации не зависит от структуры пакетов")
        void pointcutIsAnnotationDriven() {
            payments.failFirst(1);
            executor.failFirst(1);
            payments.charge("ORD-4");
            executor.execute("ORD-5");
            MatcherAssert.assertThat(
                "cannot verify that pointcut is annotation driven",
                aspect.attemptsOf("charge"),
                Matchers.equalTo(2L)
            );
            MatcherAssert.assertThat(
                "cannot verify that pointcut is annotation driven",
                aspect.attemptsOf("execute"),
                Matchers.equalTo(2L)
            );
        }
    }

    @Nested
/**
 * Слайд 124: self-invocation и три способа его обойти.
 * @since 1.0
 */
    @DisplayName("Слайд 124: self-invocation и три способа его обойти")
    final class SelfInvocation {

        @Test
        @DisplayName("Вызов через this минует прокси — ретрая нет, ошибка вылетает сразу")
        void thisCallBypassesTheProxy() {
            payments.failFirst(1);
            MatcherAssert.assertThat(
                "cannot verify that this call bypasses the proxy",
                Assertions.assertThrows(IllegalStateException.class, () -> payments.chargeViaThis("ORD-6")).getMessage(),
                Matchers.containsString("сбой платежа №1")
            );
            MatcherAssert.assertThat(
                "cannot verify that this call bypasses the proxy",
                payments.executions(),
                Matchers.equalTo(1)
            );
            MatcherAssert.assertThat(
                "cannot verify that this call bypasses the proxy",
                aspect.log().stream().anyMatch(e -> e.contains(":charge:")),
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("Обход 1: самовнедрение через ObjectProvider — ретрай работает")
        void selfInjectionRestoresTheAspect() {
            payments.failFirst(1);
            MatcherAssert.assertThat(
                "cannot verify that self injection restores the aspect",
                payments.chargeViaSelf("ORD-7"),
                Matchers.equalTo("оплачен ORD-7")
            );
            MatcherAssert.assertThat(
                "cannot verify that self injection restores the aspect",
                payments.executions(),
                Matchers.equalTo(2)
            );
            MatcherAssert.assertThat(
                "cannot verify that self injection restores the aspect",
                aspect.attemptsOf("charge"),
                Matchers.equalTo(2L)
            );
        }

        @Test
        @DisplayName("Обход 2: AopContext.currentProxy — работает, но требует exposeProxy")
        void aopContextRestoresTheAspect() {
            payments.failFirst(1);
            MatcherAssert.assertThat(
                "cannot verify that aop context restores the aspect",
                payments.chargeViaAopContext("ORD-8"),
                Matchers.equalTo("оплачен ORD-8")
            );
            MatcherAssert.assertThat(
                "cannot verify that aop context restores the aspect",
                aspect.attemptsOf("charge"),
                Matchers.equalTo(2L)
            );
        }

        @Test
        @DisplayName("Правильное решение: вынести метод в отдельный бин")
        void separateBeanIsTheCleanSolution() {
            executor.failFirst(2);
            MatcherAssert.assertThat(
                "cannot verify that separate bean is the clean solution",
                payments.chargeViaSeparateBean("ORD-9"),
                Matchers.equalTo("оплачен ORD-9")
            );
            MatcherAssert.assertThat(
                "cannot verify that separate bean is the clean solution",
                executor.executions(),
                Matchers.equalTo(3)
            );
            MatcherAssert.assertThat(
                "cannot verify that separate bean is the clean solution",
                aspect.attemptsOf("execute"),
                Matchers.equalTo(3L)
            );
        }

        @Test
        @DisplayName("Без exposeProxy обход через AopContext падает — флаг не бесплатный")
        void aopContextNeedsExposeProxy() {
            try (var plain = new AnnotationConfigApplicationContext(PlainConfig.class)) {

                final PaymentService service = plain.getBean(PaymentService.class);
                MatcherAssert.assertThat(
                    "cannot verify that aop context needs expose proxy",
                    Assertions.assertThrows(IllegalStateException.class, () -> service.chargeViaAopContext("ORD-10")).getMessage(),
                    Matchers.containsString("Cannot find current proxy")
                );
            }
        }

        /**
         * Та же конфигурация, но без {@code exposeProxy} и без сканирования пакета.
         * @since 1.0
         */
        @Configuration
        @EnableAspectJAutoProxy
        @Import({PaymentService.class, ChargeExecutor.class, RetryAspect.class})
        static class PlainConfig {
        }
    }

    @Nested
/**
 * Прокси — это другой объект, а не ваш бин.
 * @since 1.0
 */
    @DisplayName("Прокси — это другой объект, а не ваш бин")
    final class ProxyIsNotTheTarget {

        @Test
        @DisplayName("Из контекста приходит CGLIB-подкласс, а не сам PaymentService")
        void contextReturnsAProxy() {
            MatcherAssert.assertThat(
                "cannot verify that context returns a proxy",
                AopUtils.isCglibProxy(payments),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "proxy class cannot differ from the target class",
                payments.getClass(),
                Matchers.not(Matchers.equalTo(PaymentService.class))
            );
            MatcherAssert.assertThat(
                "cannot verify that context returns a proxy",
                payments.getClass().getSuperclass(),
                Matchers.equalTo(PaymentService.class)
            );
        }

        @Test
        @DisplayName("Поэтому состояние читается только методами: они делегируются цели")
        void stateIsReachableOnlyThroughMethods() {
            payments.failFirst(1);
            payments.charge("ORD-11");
            // executions() — метод, вызов делегируется настоящему бину
            MatcherAssert.assertThat(
                "cannot verify that state is reachable only through methods",
                payments.executions(),
                Matchers.equalTo(2)
            );
        }
    }
}
