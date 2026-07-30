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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            assertThat(payments.charge("ORD-1")).isEqualTo("оплачен ORD-1");
            assertThat(aspect.log()).containsExactly("success:charge:попытка1");
        }

        @Test
        @DisplayName("Аспект повторяет вызов до успеха")
        void retriesUntilSuccess() {
            payments.failFirst(2);

            assertThat(payments.charge("ORD-2")).isEqualTo("оплачен ORD-2");

            assertThat(payments.executions()).isEqualTo(3);
            assertThat(aspect.log()).containsExactly(
                    "fail:charge:попытка1", "fail:charge:попытка2", "success:charge:попытка3");
        }

        @Test
        @DisplayName("Исчерпав попытки, аспект пробрасывает последнее исключение")
        void rethrowsAfterExhaustion() {
            payments.failFirst(99);

            assertThatThrownBy(() -> payments.charge("ORD-3"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("сбой платежа №3");

            assertThat(aspect.log()).contains("exhausted:charge");
            assertThat(payments.executions()).isEqualTo(3);
        }

        @Test
        @DisplayName("Pointcut по аннотации не зависит от структуры пакетов")
        void pointcutIsAnnotationDriven() {
            payments.failFirst(1);
            executor.failFirst(1);

            payments.charge("ORD-4");
            executor.execute("ORD-5");

            assertThat(aspect.attemptsOf("charge")).isEqualTo(2);
            assertThat(aspect.attemptsOf("execute")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Слайд 124: self-invocation и три способа его обойти")
    class SelfInvocation {

        @Test
        @DisplayName("Вызов через this минует прокси — ретрая нет, ошибка вылетает сразу")
        void thisCallBypassesTheProxy() {
            payments.failFirst(1);

            assertThatThrownBy(() -> payments.chargeViaThis("ORD-6"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("сбой платежа №1");

            assertThat(payments.executions()).isEqualTo(1);
            assertThat(aspect.log()).noneMatch(e -> e.contains(":charge:"));
        }

        @Test
        @DisplayName("Обход 1: самовнедрение через ObjectProvider — ретрай работает")
        void selfInjectionRestoresTheAspect() {
            payments.failFirst(1);

            assertThat(payments.chargeViaSelf("ORD-7")).isEqualTo("оплачен ORD-7");

            assertThat(payments.executions()).isEqualTo(2);
            assertThat(aspect.attemptsOf("charge")).isEqualTo(2);
        }

        @Test
        @DisplayName("Обход 2: AopContext.currentProxy — работает, но требует exposeProxy")
        void aopContextRestoresTheAspect() {
            payments.failFirst(1);

            assertThat(payments.chargeViaAopContext("ORD-8")).isEqualTo("оплачен ORD-8");

            assertThat(aspect.attemptsOf("charge")).isEqualTo(2);
        }

        @Test
        @DisplayName("Правильное решение: вынести метод в отдельный бин")
        void separateBeanIsTheCleanSolution() {
            executor.failFirst(2);

            assertThat(payments.chargeViaSeparateBean("ORD-9")).isEqualTo("оплачен ORD-9");

            assertThat(executor.executions()).isEqualTo(3);
            assertThat(aspect.attemptsOf("execute")).isEqualTo(3);
        }

        @Test
        @DisplayName("Без exposeProxy обход через AopContext падает — флаг не бесплатный")
        void aopContextNeedsExposeProxy() {
            try (var plain = new AnnotationConfigApplicationContext(PlainConfig.class)) {
                PaymentService service = plain.getBean(PaymentService.class);

                assertThatThrownBy(() -> service.chargeViaAopContext("ORD-10"))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Cannot find current proxy");
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
            assertThat(org.springframework.aop.support.AopUtils.isCglibProxy(payments)).isTrue();
            assertThat(payments.getClass()).isNotEqualTo(PaymentService.class);
            assertThat(payments.getClass().getSuperclass()).isEqualTo(PaymentService.class);
        }

        @Test
        @DisplayName("Поэтому состояние читается только методами: они делегируются цели")
        void stateIsReachableOnlyThroughMethods() {
            payments.failFirst(1);
            payments.charge("ORD-11");

            // executions() — метод, вызов делегируется настоящему бину
            assertThat(payments.executions()).isEqualTo(2);
        }
    }
}
