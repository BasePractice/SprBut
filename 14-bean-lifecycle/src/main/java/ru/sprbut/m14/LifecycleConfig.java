/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m14;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация, поднимающая все участники жизненного цикла разом.
 * @since 1.0
 */
@Configuration
public class LifecycleConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public LifecycleConfig() {
        // нечего инициализировать
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    public static AuditBeanPostProcessor auditBeanPostProcessor() {
        // static: BeanPostProcessor должен быть создан раньше обычных бинов,
        // иначе он не успеет обработать часть из них
        return new AuditBeanPostProcessor();
    }

    /**
     * Зависимость.
     * @return Зависимость
     */
    @Bean
    public ManagedBean.Dependency dependency() {
        return new ManagedBean.Dependency();
    }

    /**
     * Объект.
     * @param dependency Зависимость
     * @return Объект
     */
    @Bean
    public ManagedBean managedBean(final ManagedBean.Dependency dependency) {
        return new ManagedBean(dependency);
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    public AuditBeanPostProcessor.AuditableBean auditableBean() {
        return new AuditBeanPostProcessor.AuditableBean();
    }

    /**
     * Значение {@code backgroundWorker}.
     * @return Значение {@code backgroundWorker}
     */
    @Bean
    public BackgroundWorker backgroundWorker() {
        return new BackgroundWorker();
    }

    /**
     * Слайд 101 напоминал: prototype-бины контейнер не уничтожает.
     * Здесь это проверяется — {@code @PreDestroy} у такого бина не вызовется.
     * @return Слайд 101 напоминал: prototype-бины контейнер не уничтожает
     */
    @Bean
    @org.springframework.context.annotation.Scope("prototype")
    public PrototypeWithDestroy prototypeWithDestroy() {
        return new PrototypeWithDestroy();
    }

    /**
     * Значение {@code PrototypeWithDestroy}.
     */
    public static class PrototypeWithDestroy {

        /**
         * Основной конструктор.
         */
        public PrototypeWithDestroy() {
            LifecycleLog.record("1-constructor:prototypeWithDestroy");
        }

        /**
         * Значение {@code preDestroy}.
         */
        @jakarta.annotation.PreDestroy
        public void preDestroy() {
            LifecycleLog.record("8a-preDestroy:prototypeWithDestroy");
        }
    }
}
