package ru.sprbut.m15.extended;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

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

@DisplayName("Расширенный пример: своя @Retryable через AOP и обход self-invocation")
class RetryAspectTest {

    private AnnotationConfigApplicationContext context;
    private PaymentService payments;
    private ChargeExecutor executor;
    private RetryAspect aspect;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(ExtendedAopConfig.class);
        payments = context.getBean(PaymentService.class);
        executor = context.getBean(ChargeExecutor.class);
        aspect = context.getBean(RetryAspect.class);
        payments.reset();
        executor.reset();
        aspect.clear();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Nested
    @DisplayName("Аннотация работает как @Transactional: метаданные + аспект")
    class Behaviour {

        @Test
        @DisplayName("Успешный вызов проходит с первой попытки")
        void succeedsImmediately() {
            assertThat(
                "cannot verify that succeeds immediately",
                payments.charge("ORD-1"),
                equalTo("оплачен ORD-1")
            );
            assertThat(
                "cannot verify that succeeds immediately",
                aspect.log(),
                contains("success:charge:попытка1")
            );
        }

        @Test
        @DisplayName("Аспект повторяет вызов до успеха")
        void retriesUntilSuccess() {
            payments.failFirst(2);

            assertThat(
                "cannot verify that retries until success",
                payments.charge("ORD-2"),
                equalTo("оплачен ORD-2")
            );

            assertThat(
                "cannot verify that retries until success",
                payments.executions(),
                equalTo(3)
            );
            assertThat(
                "cannot verify that retries until success",
                aspect.log(),
                contains( "fail:charge:попытка1", "fail:charge:попытка2", "success:charge:попытка3")
            );
        }

        @Test
        @DisplayName("Исчерпав попытки, аспект пробрасывает последнее исключение")
        void rethrowsAfterExhaustion() {
            payments.failFirst(99);

            assertThat(
                "cannot verify that rethrows after exhaustion",
                assertThrows(IllegalStateException.class, () -> payments.charge("ORD-3")).getMessage(),
                containsString("сбой платежа №3")
            );

            assertThat(
                "cannot verify that rethrows after exhaustion",
                aspect.log(),
                hasItems("exhausted:charge")
            );
            assertThat(
                "cannot verify that rethrows after exhaustion",
                payments.executions(),
                equalTo(3)
            );
        }

        @Test
        @DisplayName("Pointcut по аннотации не зависит от структуры пакетов")
        void pointcutIsAnnotationDriven() {
            payments.failFirst(1);
            executor.failFirst(1);

            payments.charge("ORD-4");
            executor.execute("ORD-5");

            assertThat(
                "cannot verify that pointcut is annotation driven",
                aspect.attemptsOf("charge"),
                equalTo(2L)
            );
            assertThat(
                "cannot verify that pointcut is annotation driven",
                aspect.attemptsOf("execute"),
                equalTo(2L)
            );
        }
    }

    @Nested
    @DisplayName("Слайд 124: self-invocation и три способа его обойти")
    class SelfInvocation {

        @Test
        @DisplayName("Вызов через this минует прокси — ретрая нет, ошибка вылетает сразу")
        void thisCallBypassesTheProxy() {
            payments.failFirst(1);

            assertThat(
                "cannot verify that this call bypasses the proxy",
                assertThrows(IllegalStateException.class, () -> payments.chargeViaThis("ORD-6")).getMessage(),
                containsString("сбой платежа №1")
            );

            assertThat(
                "cannot verify that this call bypasses the proxy",
                payments.executions(),
                equalTo(1)
            );
            assertThat(
                "cannot verify that this call bypasses the proxy",
                aspect.log().stream().anyMatch(e -> e.contains(":charge:")),
                equalTo(false)
            );
        }

        @Test
        @DisplayName("Обход 1: самовнедрение через ObjectProvider — ретрай работает")
        void selfInjectionRestoresTheAspect() {
            payments.failFirst(1);

            assertThat(
                "cannot verify that self injection restores the aspect",
                payments.chargeViaSelf("ORD-7"),
                equalTo("оплачен ORD-7")
            );

            assertThat(
                "cannot verify that self injection restores the aspect",
                payments.executions(),
                equalTo(2)
            );
            assertThat(
                "cannot verify that self injection restores the aspect",
                aspect.attemptsOf("charge"),
                equalTo(2L)
            );
        }

        @Test
        @DisplayName("Обход 2: AopContext.currentProxy — работает, но требует exposeProxy")
        void aopContextRestoresTheAspect() {
            payments.failFirst(1);

            assertThat(
                "cannot verify that aop context restores the aspect",
                payments.chargeViaAopContext("ORD-8"),
                equalTo("оплачен ORD-8")
            );

            assertThat(
                "cannot verify that aop context restores the aspect",
                aspect.attemptsOf("charge"),
                equalTo(2L)
            );
        }

        @Test
        @DisplayName("Правильное решение: вынести метод в отдельный бин")
        void separateBeanIsTheCleanSolution() {
            executor.failFirst(2);

            assertThat(
                "cannot verify that separate bean is the clean solution",
                payments.chargeViaSeparateBean("ORD-9"),
                equalTo("оплачен ORD-9")
            );

            assertThat(
                "cannot verify that separate bean is the clean solution",
                executor.executions(),
                equalTo(3)
            );
            assertThat(
                "cannot verify that separate bean is the clean solution",
                aspect.attemptsOf("execute"),
                equalTo(3L)
            );
        }

        @Test
        @DisplayName("Без exposeProxy обход через AopContext падает — флаг не бесплатный")
        void aopContextNeedsExposeProxy() {
            try (var plain = new AnnotationConfigApplicationContext(PlainConfig.class)) {
                PaymentService service = plain.getBean(PaymentService.class);

                assertThat(
                    "cannot verify that aop context needs expose proxy",
                    assertThrows(IllegalStateException.class, () -> service.chargeViaAopContext("ORD-10")).getMessage(),
                    containsString("Cannot find current proxy")
                );
            }
        }

        /** Та же конфигурация, но без {@code exposeProxy} и без сканирования пакета. */
        @Configuration
        @EnableAspectJAutoProxy
        @Import({PaymentService.class, ChargeExecutor.class, RetryAspect.class})
        static class PlainConfig {
        }
    }

    @Nested
    @DisplayName("Прокси — это другой объект, а не ваш бин")
    class ProxyIsNotTheTarget {

        @Test
        @DisplayName("Из контекста приходит CGLIB-подкласс, а не сам PaymentService")
        void contextReturnsAProxy() {
            assertThat(
                "cannot verify that context returns a proxy",
                org.springframework.aop.support.AopUtils.isCglibProxy(payments),
                equalTo(true)
            );
            assertThat(
                "proxy class cannot differ from the target class",
                payments.getClass(),
                not(equalTo(PaymentService.class))
            );
            assertThat(
                "cannot verify that context returns a proxy",
                payments.getClass().getSuperclass(),
                equalTo(PaymentService.class)
            );
        }

        @Test
        @DisplayName("Поэтому состояние читается только методами: они делегируются цели")
        void stateIsReachableOnlyThroughMethods() {
            payments.failFirst(1);
            payments.charge("ORD-11");

            // executions() — метод, вызов делегируется настоящему бину
            assertThat(
                "cannot verify that state is reachable only through methods",
                payments.executions(),
                equalTo(2)
            );
        }
    }
}
