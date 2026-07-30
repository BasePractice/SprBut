package ru.sprbut.m14;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Слайды 111–118 (СХЕМА 7): восемь шагов жизненного цикла бина")
class BeanLifecycleTest {

    @BeforeEach
    void clearLog() {
        LifecycleLog.clear();
    }

    @Nested
    @DisplayName("Порядок шагов 1–7 при старте контекста")
    class Startup {

        @Test
        @DisplayName("Все восемь шагов проходятся строго по порядку")
        void stepsHappenInOrder() {
            try (var context = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                context.getBean(ManagedBean.class);

                assertThat(LifecycleLog.eventsOf("managedBean")).containsExactly(
                        "1-constructor:managedBean",
                        "2-dependencies:managedBean",
                        "3-aware-beanName:managedBean",
                        "3-aware-beanFactory:managedBean",
                        "3-aware-applicationContext:managedBean",
                        "4-bpp-before:managedBean",
                        "5a-postConstruct:managedBean",
                        "5b-afterPropertiesSet:managedBean",
                        "6-bpp-after:managedBean");
            }
        }

        @Test
        @DisplayName("Шаг 2: зависимость создаётся раньше того, кому она нужна")
        void dependenciesComeFirst() {
            try (var ignored = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                assertThat(LifecycleLog.indexOf("0-dependency-created:dependency"))
                        .isLessThan(LifecycleLog.indexOf("1-constructor:managedBean"));
            }
        }

        @Test
        @DisplayName("Шаг 3: *Aware-интерфейсы отдают бину сведения о контейнере")
        void awareInterfacesAreCalled() {
            try (var context = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                ManagedBean bean = context.getBean(ManagedBean.class);

                assertThat(bean.beanName()).isEqualTo("managedBean");
                assertThat(bean.fullyAware()).isTrue();
            }
        }

        @Test
        @DisplayName("Шаг 5: @PostConstruct вызывается раньше afterPropertiesSet")
        void postConstructPrecedesAfterPropertiesSet() {
            try (var ignored = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                assertThat(LifecycleLog.indexOf("5a-postConstruct:managedBean"))
                        .isLessThan(LifecycleLog.indexOf("5b-afterPropertiesSet:managedBean"));
            }
        }

        @Test
        @DisplayName("Шаги 4 и 6: BeanPostProcessor обрамляет инициализацию с двух сторон")
        void beanPostProcessorWrapsInitialization() {
            try (var ignored = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                assertThat(LifecycleLog.indexOf("4-bpp-before:managedBean"))
                        .isLessThan(LifecycleLog.indexOf("5a-postConstruct:managedBean"));
                assertThat(LifecycleLog.indexOf("6-bpp-after:managedBean"))
                        .isGreaterThan(LifecycleLog.indexOf("5b-afterPropertiesSet:managedBean"));
            }
        }

        @Test
        @DisplayName("Шаг 7: SmartLifecycle.start вызывается после готовности контекста")
        void smartLifecycleStartsLast() {
            try (var context = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                assertThat(context.getBean(BackgroundWorker.class).isRunning()).isTrue();
                assertThat(LifecycleLog.indexOf("7-smartLifecycle-start:backgroundWorker"))
                        .isGreaterThan(LifecycleLog.indexOf("6-bpp-after:managedBean"));
            }
        }
    }

    @Nested
    @DisplayName("Шаг 8: уничтожение")
    class Shutdown {

        @Test
        @DisplayName("@PreDestroy вызывается раньше DisposableBean.destroy")
        void preDestroyPrecedesDestroy() {
            var context = new AnnotationConfigApplicationContext(LifecycleConfig.class);
            context.getBean(ManagedBean.class);
            context.close();

            assertThat(LifecycleLog.indexOf("8a-preDestroy:managedBean"))
                    .isLessThan(LifecycleLog.indexOf("8b-destroy:managedBean"));
        }

        @Test
        @DisplayName("SmartLifecycle.stop вызывается раньше уничтожения бинов")
        void stopPrecedesDestruction() {
            var context = new AnnotationConfigApplicationContext(LifecycleConfig.class);
            context.close();

            assertThat(LifecycleLog.indexOf("9-smartLifecycle-stop:backgroundWorker"))
                    .isLessThan(LifecycleLog.indexOf("8a-preDestroy:managedBean"));
        }

        @Test
        @DisplayName("У prototype-бина @PreDestroy не вызывается никогда")
        void prototypeIsNeverDestroyed() {
            var context = new AnnotationConfigApplicationContext(LifecycleConfig.class);
            context.getBean(LifecycleConfig.PrototypeWithDestroy.class);
            context.close();

            assertThat(LifecycleLog.events())
                    .contains("1-constructor:prototypeWithDestroy")
                    .doesNotContain("8a-preDestroy:prototypeWithDestroy");
        }
    }

    @Nested
    @DisplayName("Шаг 6 умеет подменить объект")
    class BeanReplacement {

        @Test
        @DisplayName("В контексте лежит прокси, а не тот объект, который создал @Bean-метод")
        void postProcessorCanReturnADifferentObject() {
            try (var context = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                AuditBeanPostProcessor.Auditable bean =
                        context.getBean(AuditBeanPostProcessor.Auditable.class);

                assertThat(bean.describe()).isEqualTo("оригинал (через прокси)");
                assertThat(java.lang.reflect.Proxy.isProxyClass(bean.getClass())).isTrue();
                assertThat(bean).isNotInstanceOf(AuditBeanPostProcessor.AuditableBean.class);
            }
        }

        @Test
        @DisplayName("Именно так Spring и подставляет AOP-прокси на место бина")
        void thisIsHowSpringAopWorks() {
            try (var context = new AnnotationConfigApplicationContext(LifecycleConfig.class)) {
                assertThat(LifecycleLog.events()).contains("6-bpp-after:auditableBean");
                assertThat(context.getBean("auditableBean"))
                        .isNotInstanceOf(AuditBeanPostProcessor.AuditableBean.class);
            }
        }
    }
}
