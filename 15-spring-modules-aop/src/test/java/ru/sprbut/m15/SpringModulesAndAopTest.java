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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайды 119–126 (СХЕМЫ 8 и 9): модули Spring и AOP")
class SpringModulesAndAopTest {

    @Nested
    @DisplayName("СХЕМА 8 (слайд 120): карта модулей")
    class ModuleMap {

        @Test
        @DisplayName("В основании карты — spring-core, он ни от чего не зависит")
        void coreIsTheFoundation() {
            assertThat(
                "cannot verify that core is the foundation",
                SpringModuleMap.foundation(),
                contains("spring-core")
            );
            assertThat(
                "cannot verify that core is the foundation",
                SpringModuleMap.CORE.dependsOn(),
                empty()
            );
        }

        @Test
        @DisplayName("Всё остальное зависит от core прямо или транзитивно")
        void everythingDependsOnCore() {
            assertThat(
                "every module cannot depend on spring-core",
                SpringModuleMap.all().stream()
                    .filter(module -> !module.name().equals("spring-core"))
                    .allMatch(module ->
                        SpringModuleMap.transitiveDependencies(module.name()).contains("spring-core")
                    ),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("Boot и Cloud — верхний слой платформы")
        void bootAndCloudAreOnTop() {
            assertThat(
                "cannot verify that boot and cloud are on top",
                SpringModuleMap.BOOT.layer(),
                equalTo(SpringModuleMap.Layer.PLATFORM)
            );
            assertThat(
                "cannot verify that boot and cloud are on top",
                SpringModuleMap.transitiveDependencies("spring-cloud"),
                hasItems("spring-boot", "spring-context", "spring-core")
            );
        }

        @Test
        @DisplayName("Транзакции и безопасность построены на AOP — отсюда все их особенности")
        void transactionsAndSecurityAreBuiltOnAop() {
            assertThat(
                "cannot verify that transactions and security are built on aop",
                SpringModuleMap.builtOnAop(),
                hasItems("spring-aop", "spring-tx", "spring-jdbc", "spring-data", "spring-security")
            );
        }

        @Test
        @DisplayName("Транзитивные зависимости раскрываются полностью")
        void transitiveDependenciesAreComplete() {
            assertThat(
                "cannot verify that transitive dependencies are complete",
                SpringModuleMap.transitiveDependencies("spring-jdbc"),
                containsInAnyOrder("spring-core", "spring-beans", "spring-tx", "spring-aop")
            );
        }
    }

    @Nested
    @DisplayName("СХЕМА 9 (слайд 126): прокси вокруг бина")
    class Proxies {

        private AnnotationConfigApplicationContext context;
        private AuditAspect aspect;

        @BeforeEach
        void setUp() {
            context = new AnnotationConfigApplicationContext(AopConfig.class);
            aspect = context.getBean(AuditAspect.class);
            aspect.clear();
        }

        @AfterEach
        void tearDown() {
            context.close();
        }

        @Test
        @DisplayName("Класс не меняется — меняется то, что лежит в контейнере")
        void targetClassIsUntouched() {
            PricingService bean = context.getBean(PricingService.class);

            assertThat(
                "cannot verify that target class is untouched",
                AopUtils.isAopProxy(bean),
                equalTo(true)
            );
            assertThat(
                "proxy class cannot differ from the target class",
                bean.getClass(),
                not(equalTo(PricingService.class))
            );
            assertThat(
                "cannot verify that target class is untouched",
                PricingService.class.getSuperclass(),
                equalTo(Object.class)
            );
        }

        @Test
        @DisplayName("Слайд 123: нет интерфейса — CGLIB-подкласс")
        void classWithoutInterfaceGetsCglibProxy() {
            PricingService bean = context.getBean(PricingService.class);

            assertThat(
                "cannot verify that class without interface gets cglib proxy",
                AopUtils.isCglibProxy(bean),
                equalTo(true)
            );
            assertThat(
                "cannot verify that class without interface gets cglib proxy",
                bean.getClass().getSuperclass(),
                equalTo(PricingService.class)
            );
        }

        @Test
        @DisplayName("Слайд 122: есть интерфейс — JDK dynamic proxy")
        void classWithInterfaceGetsJdkProxy() {
            DiscountService bean = context.getBean(DiscountService.class);

            assertThat(
                "cannot verify that class with interface gets jdk proxy",
                AopUtils.isJdkDynamicProxy(bean),
                equalTo(true)
            );
            assertThat(
                "cannot verify that class with interface gets jdk proxy",
                java.lang.reflect.Proxy.isProxyClass(bean.getClass()),
                equalTo(true)
            );
        }

        @Test
        @DisplayName("JDK-прокси нельзя привести к классу реализации — частая ошибка")
        void jdkProxyIsNotTheImplementationClass() {
            assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, () -> context.getBean(StandardDiscountService.class));

            assertThat(
                "JDK proxy cannot avoid being the implementation class",
                context.getBean(DiscountService.class) instanceof StandardDiscountService,
                equalTo(false)
            );
        }

        @Test
        @DisplayName("@Before и @Around срабатывают на внешнем вызове")
        void adviceRunsOnExternalCall() {
            context.getBean(PricingService.class).calculate(new BigDecimal("100"));

            // Порядок @Before и @Around внутри одного аспекта не определён,
            // а вот @Around обязан обрамлять вызов с двух сторон
            assertThat(
                "cannot verify that advice runs on external call",
                aspect.log(),
                containsInAnyOrder( "before:calculate", "around-start:calculate", "around-end:calculate")
            );
            assertThat(
                "around advice cannot wrap the call from both sides",
                aspect.log().indexOf("around-start:calculate"),
                lessThan(aspect.log().indexOf("around-end:calculate"))
            );
        }

        @Test
        @DisplayName("@AfterThrowing видит исключение, но не гасит его")
        void afterThrowingObservesButDoesNotSwallow() {
            PricingService service = context.getBean(PricingService.class);

            assertThrows(IllegalArgumentException.class, () -> service.failing(BigDecimal.TEN));

            assertThat(
                "cannot verify that after throwing observes but does not swallow",
                aspect.log(),
                hasItems("afterThrowing:IllegalArgumentException")
            );
        }

        @Test
        @DisplayName("Слайд 124: self-invocation не перехватывается")
        void selfInvocationIsNotIntercepted() {
            PricingService service = context.getBean(PricingService.class);
            service.reset();

            service.calculateTwice(new BigDecimal("100"));

            // тело calculate выполнилось дважды...
            assertThat(
                "cannot verify that self invocation is not intercepted",
                service.calls(),
                equalTo(2)
            );
            // ...но аспект не увидел ни одного вызова calculate
            assertThat(
                "self invocation cannot bypass the aspect",
                aspect.log(),
                not(hasItem("before:calculate"))
            );
        }

        @Test
        @DisplayName("Тот же метод, вызванный снаружи, перехватывается нормально")
        void externalCallOfTheSameMethodIsIntercepted() {
            context.getBean(PricingService.class).calculate(new BigDecimal("100"));

            assertThat(
                "cannot verify that external call of the same method is intercepted",
                aspect.log(),
                hasItems("before:calculate")
            );
        }
    }
}
