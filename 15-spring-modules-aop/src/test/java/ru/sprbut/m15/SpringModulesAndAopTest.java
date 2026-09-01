/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m15;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.sprbut.m15.aop.AopConfig;
import ru.sprbut.m15.aop.AuditAspect;
import ru.sprbut.m15.aop.DiscountService;
import ru.sprbut.m15.aop.PricingService;
import ru.sprbut.m15.aop.StandardDiscountService;
import ru.sprbut.m15.modules.SpringModuleMap;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

/**
 * Слайды 119–126 (СХЕМЫ 8 и 9): модули Spring и AOP.
 * @since 1.0
 */
@DisplayName("Слайды 119–126 (СХЕМЫ 8 и 9): модули Spring и AOP")
class SpringModulesAndAopTest {

    @Nested
/**
 * СХЕМА 8 (слайд 120): карта модулей.
 * @since 1.0
 */
    @DisplayName("СХЕМА 8 (слайд 120): карта модулей")
    class ModuleMap {

        @Test
        @DisplayName("В основании карты — spring-core, он ни от чего не зависит")
        void coreIsTheFoundation() {
            MatcherAssert.assertThat(
                "cannot verify that core is the foundation",
                SpringModuleMap.foundation(),
                Matchers.contains("spring-core")
            );
            MatcherAssert.assertThat(
                "cannot verify that core is the foundation",
                SpringModuleMap.CORE.dependsOn(),
                Matchers.empty()
            );
        }

        @Test
        @DisplayName("Всё остальное зависит от core прямо или транзитивно")
        void everythingDependsOnCore() {
            MatcherAssert.assertThat(
                "every module cannot depend on spring-core",
                SpringModuleMap.all().stream()
                    .filter(module -> !module.name().equals("spring-core"))
                    .allMatch(module ->
                        SpringModuleMap.transitiveDependencies(module.name()).contains("spring-core")
                    ),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("Boot и Cloud — верхний слой платформы")
        void bootAndCloudAreOnTop() {
            MatcherAssert.assertThat(
                "cannot verify that boot and cloud are on top",
                SpringModuleMap.BOOT.layer(),
                Matchers.equalTo(SpringModuleMap.Layer.PLATFORM)
            );
            MatcherAssert.assertThat(
                "cannot verify that boot and cloud are on top",
                SpringModuleMap.transitiveDependencies("spring-cloud"),
                Matchers.hasItems("spring-boot", "spring-context", "spring-core")
            );
        }

        @Test
        @DisplayName("Транзакции и безопасность построены на AOP — отсюда все их особенности")
        void transactionsAndSecurityAreBuiltOnAop() {
            MatcherAssert.assertThat(
                "cannot verify that transactions and security are built on aop",
                SpringModuleMap.builtOnAop(),
                Matchers.hasItems("spring-aop", "spring-tx", "spring-jdbc", "spring-data", "spring-security")
            );
        }

        @Test
        @DisplayName("Транзитивные зависимости раскрываются полностью")
        void transitiveDependenciesAreComplete() {
            MatcherAssert.assertThat(
                "cannot verify that transitive dependencies are complete",
                SpringModuleMap.transitiveDependencies("spring-jdbc"),
                Matchers.containsInAnyOrder("spring-core", "spring-beans", "spring-tx", "spring-aop")
            );
        }
    }

    @Nested
/**
 * СХЕМА 9 (слайд 126): прокси вокруг бина.
 * @since 1.0
 */
    @DisplayName("СХЕМА 9 (слайд 126): прокси вокруг бина")
    class Proxies {

        /**
         * Контекст.
         */
        private AnnotationConfigApplicationContext context;
        /**
         * Значение {@code aspect}.
         */
        private AuditAspect aspect;

        @BeforeEach
        void setUp() {
            this.context = new AnnotationConfigApplicationContext(AopConfig.class);
            this.aspect = this.context.getBean(AuditAspect.class);
            this.aspect.clear();
        }

        @AfterEach
        void tearDown() {
            this.context.close();
        }

        @Test
        @DisplayName("Класс не меняется — меняется то, что лежит в контейнере")
        void targetClassIsUntouched() {
            final PricingService bean = this.context.getBean(PricingService.class);
            MatcherAssert.assertThat(
                "cannot verify that target class is untouched",
                AopUtils.isAopProxy(bean),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "proxy class cannot differ from the target class",
                bean.getClass(),
                Matchers.not(Matchers.equalTo(PricingService.class))
            );
            MatcherAssert.assertThat(
                "cannot verify that target class is untouched",
                PricingService.class.getSuperclass(),
                Matchers.equalTo(Object.class)
            );
        }

        @Test
        @DisplayName("Слайд 123: нет интерфейса — CGLIB-подкласс")
        void classWithoutInterfaceGetsCglibProxy() {
            final PricingService bean = this.context.getBean(PricingService.class);
            MatcherAssert.assertThat(
                "cannot verify that class without interface gets cglib proxy",
                AopUtils.isCglibProxy(bean),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that class without interface gets cglib proxy",
                bean.getClass().getSuperclass(),
                Matchers.equalTo(PricingService.class)
            );
        }

        @Test
        @DisplayName("Слайд 122: есть интерфейс — JDK dynamic proxy")
        void classWithInterfaceGetsJdkProxy() {
            final DiscountService bean = this.context.getBean(DiscountService.class);
            MatcherAssert.assertThat(
                "cannot verify that class with interface gets jdk proxy",
                AopUtils.isJdkDynamicProxy(bean),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                "cannot verify that class with interface gets jdk proxy",
                java.lang.reflect.Proxy.isProxyClass(bean.getClass()),
                Matchers.equalTo(true)
            );
        }

        @Test
        @DisplayName("JDK-прокси нельзя привести к классу реализации — частая ошибка")
        void jdkProxyIsNotTheImplementationClass() {
            Assertions.assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, () -> this.context.getBean(StandardDiscountService.class));
            MatcherAssert.assertThat(
                "JDK proxy cannot avoid being the implementation class",
                this.context.getBean(DiscountService.class) instanceof StandardDiscountService,
                Matchers.equalTo(false)
            );
        }

        @Test
        @DisplayName("@Before и @Around срабатывают на внешнем вызове")
        void adviceRunsOnExternalCall() {
            this.context.getBean(PricingService.class).calculate(new BigDecimal("100"));
            // Порядок @Before и @Around внутри одного аспекта не определён,
            // а вот @Around обязан обрамлять вызов с двух сторон
            MatcherAssert.assertThat(
                "cannot verify that advice runs on external call",
                this.aspect.log(),
                Matchers.containsInAnyOrder( "before:calculate", "around-start:calculate", "around-end:calculate")
            );
            MatcherAssert.assertThat(
                "around advice cannot wrap the call from both sides",
                this.aspect.log().indexOf("around-start:calculate"),
                Matchers.lessThan(this.aspect.log().indexOf("around-end:calculate"))
            );
        }

        @Test
        @DisplayName("@AfterThrowing видит исключение, но не гасит его")
        void afterThrowingObservesButDoesNotSwallow() {
            final PricingService service = this.context.getBean(PricingService.class);
            Assertions.assertThrows(IllegalArgumentException.class, () -> service.failing(BigDecimal.TEN));
            MatcherAssert.assertThat(
                "cannot verify that after throwing observes but does not swallow",
                this.aspect.log(),
                Matchers.hasItems("afterThrowing:IllegalArgumentException")
            );
        }

        @Test
        @DisplayName("Слайд 124: self-invocation не перехватывается")
        void selfInvocationIsNotIntercepted() {
            final PricingService service = this.context.getBean(PricingService.class);
            service.reset();
            service.calculateTwice(new BigDecimal("100"));
            // тело calculate выполнилось дважды...
            MatcherAssert.assertThat(
                "cannot verify that self invocation is not intercepted",
                service.calls(),
                Matchers.equalTo(2)
            );
            // ...но аспект не увидел ни одного вызова calculate
            MatcherAssert.assertThat(
                "self invocation cannot bypass the aspect",
                this.aspect.log(),
                Matchers.not(Matchers.hasItem("before:calculate"))
            );
        }

        @Test
        @DisplayName("Тот же метод, вызванный снаружи, перехватывается нормально")
        void externalCallOfTheSameMethodIsIntercepted() {
            this.context.getBean(PricingService.class).calculate(new BigDecimal("100"));
            MatcherAssert.assertThat(
                "cannot verify that external call of the same method is intercepted",
                this.aspect.log(),
                Matchers.hasItems("before:calculate")
            );
        }
    }
}
