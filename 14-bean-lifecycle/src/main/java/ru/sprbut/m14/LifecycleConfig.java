/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// prototype-бин показывает, что контейнер его не уничтожает,
// и живёт рядом с конфигурацией, которая его объявляет
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m14;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
     * Обработчик бинов.
     *
     * <p>Метод статический: {@code BeanPostProcessor} должен быть создан раньше
     * обычных бинов, иначе он не успеет обработать часть из них.</p>
     *
     * @return Обработчик бинов
     */
    @Bean
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static AuditBeanPostProcessor auditBeanPostProcessor() {
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
     * @return Prototype-бин с методом уничтожения
     */
    @Bean
    @Scope("prototype")
    public LifecycleConfig.PrototypeWithDestroy prototypeWithDestroy() {
        return new LifecycleConfig.PrototypeWithDestroy();
    }

    /**
     * Prototype-бин с методом уничтожения, который контейнер не вызовет.
     * @since 1.0
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public static final class PrototypeWithDestroy {

        /**
         * Основной конструктор: сама запись в журнал и есть шаг 1.
         * @checkstyle ConstructorsCodeFreeCheck (4 lines)
         */
        public PrototypeWithDestroy() {
            LifecycleLog.record("1-constructor:prototypeWithDestroy");
        }

        /**
         * Шаг 8а, до которого prototype-бин не доживает.
         * @checkstyle NonStaticMethodCheck (4 lines)
         */
        @PreDestroy
        public void preDestroy() {
            LifecycleLog.record("8a-preDestroy:prototypeWithDestroy");
        }
    }
}
