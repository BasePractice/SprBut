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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайды 119–126 (СХЕМЫ 8 и 9): модули Spring и AOP")
class SpringModulesAndAopTest {

    @Nested
    @DisplayName("СХЕМА 8 (слайд 120): карта модулей")
    class ModuleMap {

        @Test
        @DisplayName("В основании карты — spring-core, он ни от чего не зависит")
        void coreIsTheFoundation() {
            assertThat(SpringModuleMap.foundation()).containsExactly("spring-core");
            assertThat(SpringModuleMap.CORE.dependsOn()).isEmpty();
        }

        @Test
        @DisplayName("Всё остальное зависит от core прямо или транзитивно")
        void everythingDependsOnCore() {
            assertThat(SpringModuleMap.all())
                    .filteredOn(m -> !m.name().equals("spring-core"))
                    .allSatisfy(m -> assertThat(SpringModuleMap.transitiveDependencies(m.name()))
                            .contains("spring-core"));
        }

        @Test
        @DisplayName("Boot и Cloud — верхний слой платформы")
        void bootAndCloudAreOnTop() {
            assertThat(SpringModuleMap.BOOT.layer()).isEqualTo(SpringModuleMap.Layer.PLATFORM);
            assertThat(SpringModuleMap.transitiveDependencies("spring-cloud"))
                    .contains("spring-boot", "spring-context", "spring-core");
        }

        @Test
        @DisplayName("Транзакции и безопасность построены на AOP — отсюда все их особенности")
        void transactionsAndSecurityAreBuiltOnAop() {
            assertThat(SpringModuleMap.builtOnAop())
                    .contains("spring-aop", "spring-tx", "spring-jdbc", "spring-data",
                            "spring-security");
        }

        @Test
        @DisplayName("Транзитивные зависимости раскрываются полностью")
        void transitiveDependenciesAreComplete() {
            assertThat(SpringModuleMap.transitiveDependencies("spring-jdbc"))
                    .containsExactlyInAnyOrder("spring-core", "spring-beans", "spring-tx", "spring-aop");
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

            assertThat(AopUtils.isAopProxy(bean)).isTrue();
            assertThat(bean.getClass()).isNotEqualTo(PricingService.class);
            assertThat(PricingService.class.getSuperclass()).isEqualTo(Object.class);
        }

        @Test
        @DisplayName("Слайд 123: нет интерфейса — CGLIB-подкласс")
        void classWithoutInterfaceGetsCglibProxy() {
            PricingService bean = context.getBean(PricingService.class);

            assertThat(AopUtils.isCglibProxy(bean)).isTrue();
            assertThat(bean.getClass().getSuperclass()).isEqualTo(PricingService.class);
        }

        @Test
        @DisplayName("Слайд 122: есть интерфейс — JDK dynamic proxy")
        void classWithInterfaceGetsJdkProxy() {
            DiscountService bean = context.getBean(DiscountService.class);

            assertThat(AopUtils.isJdkDynamicProxy(bean)).isTrue();
            assertThat(java.lang.reflect.Proxy.isProxyClass(bean.getClass())).isTrue();
        }

        @Test
        @DisplayName("JDK-прокси нельзя привести к классу реализации — частая ошибка")
        void jdkProxyIsNotTheImplementationClass() {
            assertThatThrownBy(() -> context.getBean(StandardDiscountService.class))
                    .isInstanceOf(org.springframework.beans.factory.NoSuchBeanDefinitionException.class);

            assertThat(context.getBean(DiscountService.class))
                    .isNotInstanceOf(StandardDiscountService.class);
        }

        @Test
        @DisplayName("@Before и @Around срабатывают на внешнем вызове")
        void adviceRunsOnExternalCall() {
            context.getBean(PricingService.class).calculate(new BigDecimal("100"));

            // Порядок @Before и @Around внутри одного аспекта не определён,
            // а вот @Around обязан обрамлять вызов с двух сторон
            assertThat(aspect.log()).containsExactlyInAnyOrder(
                    "before:calculate", "around-start:calculate", "around-end:calculate");
            assertThat(aspect.log().indexOf("around-start:calculate"))
                    .isLessThan(aspect.log().indexOf("around-end:calculate"));
        }

        @Test
        @DisplayName("@AfterThrowing видит исключение, но не гасит его")
        void afterThrowingObservesButDoesNotSwallow() {
            PricingService service = context.getBean(PricingService.class);

            assertThatThrownBy(() -> service.failing(BigDecimal.TEN))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(aspect.log()).contains("afterThrowing:IllegalArgumentException");
        }

        @Test
        @DisplayName("Слайд 124: self-invocation не перехватывается")
        void selfInvocationIsNotIntercepted() {
            PricingService service = context.getBean(PricingService.class);
            service.reset();

            service.calculateTwice(new BigDecimal("100"));

            // тело calculate выполнилось дважды...
            assertThat(service.calls()).isEqualTo(2);
            // ...но аспект не увидел ни одного вызова calculate
            assertThat(aspect.log())
                    .contains("before:calculateTwice")
                    .doesNotContain("before:calculate")
                    .doesNotContain("around-start:calculate");
        }

        @Test
        @DisplayName("Тот же метод, вызванный снаружи, перехватывается нормально")
        void externalCallOfTheSameMethodIsIntercepted() {
            context.getBean(PricingService.class).calculate(new BigDecimal("100"));

            assertThat(aspect.log()).contains("before:calculate");
        }
    }
}
