/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m14;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * Бин, реализующий <b>все</b> точки расширения жизненного цикла сразу —
 * чтобы восемь шагов со слайда 118 (СХЕМА 7) можно было увидеть в одном списке:
 * <ol>
 * <li>создание экземпляра (конструктор);</li>
 * <li>внедрение зависимостей;</li>
 * <li>*Aware-интерфейсы;</li>
 * <li>{@code BeanPostProcessor.postProcessBeforeInitialization};</li>
 * <li>{@code @PostConstruct}, затем {@code afterPropertiesSet};</li>
 * <li>{@code BeanPostProcessor.postProcessAfterInitialization};</li>
 * <li>бин готов; {@code SmartLifecycle.start};</li>
 * <li>{@code @PreDestroy}, затем {@code DisposableBean.destroy}.</li>
 * </ol>
 * В реальном коде столько интерфейсов сразу не реализуют: {@code @PostConstruct}
 * и {@code @PreDestroy} предпочтительнее, потому что не привязывают класс к Spring.
 * @since 1.0
 */
public class ManagedBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware,
        InitializingBean, DisposableBean {

    /**
     * Зависимость.
     */
    private final Dependency dependency;
    /**
     * Объект.
     */
    private String beanName;
    /**
     * Объект.
     */
    private boolean beanFactoryInjected;
    /**
     * Контекст.
     */
    private boolean contextInjected;

    /**
     * Шаг 1: конструктор. Шаг 2: зависимость приходит вместе с ним.
     * @param dependency Зависимость
     */
    public ManagedBean(final Dependency dependency) {
        this.dependency = dependency;
        LifecycleLog.record("1-constructor:managedBean");
        if (dependency != null) {
            LifecycleLog.record("2-dependencies:managedBean");
        }
    }

    /**
     * Шаг 3: *Aware-интерфейсы. Контейнер отдаёт бину сведения о себе.
     */
    @Override
    public void setBeanName(final @NonNull String name) {
        this.beanName = name;
        LifecycleLog.record("3-aware-beanName:managedBean");
    }

    @Override
    public void setBeanFactory(final @NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactoryInjected = beanFactory != null;
        LifecycleLog.record("3-aware-beanFactory:managedBean");
    }

    @Override
    public void setApplicationContext(final @NonNull ApplicationContext context) throws BeansException {
        this.contextInjected = context != null;
        LifecycleLog.record("3-aware-applicationContext:managedBean");
    }

    /**
     * Шаг 5а: {@code @PostConstruct} вызывается раньше {@code afterPropertiesSet}.
     */
    @PostConstruct
    public void postConstruct() {
        LifecycleLog.record("5a-postConstruct:managedBean");
    }

    /**
     * Шаг 5б: контракт {@link InitializingBean}.
     */
    @Override
    public void afterPropertiesSet() {
        LifecycleLog.record("5b-afterPropertiesSet:managedBean");
    }

    /**
     * Шаг 8а: {@code @PreDestroy} вызывается раньше {@code destroy}.
     */
    @PreDestroy
    public void preDestroy() {
        LifecycleLog.record("8a-preDestroy:managedBean");
    }

    /**
     * Шаг 8б: контракт {@link DisposableBean}.
     */
    @Override
    public void destroy() {
        LifecycleLog.record("8b-destroy:managedBean");
    }

    /**
     * Объект.
     * @return Объект
     */
    public String beanName() {
        return this.beanName;
    }

    /**
     * Значение {@code fullyAware}.
     * @return Значение {@code fullyAware}
     */
    public boolean fullyAware() {
        return this.beanName != null && this.beanFactoryInjected && this.contextInjected;
    }

    /**
     * Работа.
     * @return Работа
     */
    public String work() {
        return "работаю с " + this.dependency.name();
    }

    /**
     * Простая зависимость, чтобы шаг 2 был не гипотетическим.
     */
    public static class Dependency {

        /**
         * Основной конструктор.
         */
        public Dependency() {
            LifecycleLog.record("0-dependency-created:dependency");
        }

        /**
         * Имя.
         * @return Имя
         */
        public String name() {
            return "зависимость";
        }
    }
}
