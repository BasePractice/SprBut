package ru.sprbut.m14;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация, поднимающая все участники жизненного цикла разом.
 */
@Configuration
public class LifecycleConfig {

    @Bean
    public static AuditBeanPostProcessor auditBeanPostProcessor() {
        // static: BeanPostProcessor должен быть создан раньше обычных бинов,
        // иначе он не успеет обработать часть из них
        return new AuditBeanPostProcessor();
    }

    @Bean
    public ManagedBean.Dependency dependency() {
        return new ManagedBean.Dependency();
    }

    @Bean
    public ManagedBean managedBean(ManagedBean.Dependency dependency) {
        return new ManagedBean(dependency);
    }

    @Bean
    public AuditBeanPostProcessor.AuditableBean auditableBean() {
        return new AuditBeanPostProcessor.AuditableBean();
    }

    @Bean
    public BackgroundWorker backgroundWorker() {
        return new BackgroundWorker();
    }

    /**
     * Слайд 101 напоминал: prototype-бины контейнер не уничтожает.
     * Здесь это проверяется — {@code @PreDestroy} у такого бина не вызовется.
     */
    @Bean
    @org.springframework.context.annotation.Scope("prototype")
    public PrototypeWithDestroy prototypeWithDestroy() {
        return new PrototypeWithDestroy();
    }

    public static class PrototypeWithDestroy {

        public PrototypeWithDestroy() {
            LifecycleLog.record("1-constructor:prototypeWithDestroy");
        }

        @jakarta.annotation.PreDestroy
        public void preDestroy() {
            LifecycleLog.record("8a-preDestroy:prototypeWithDestroy");
        }
    }
}
