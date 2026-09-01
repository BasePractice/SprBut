/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// имена полей и параметров идут от контрактов *Aware, менять их нельзя;
// запись в журнал прямо из конструктора и есть шаг 1 жизненного цикла
// @checkstyle MemberNameCheck disable
// @checkstyle ParameterNameCheck disable
// @checkstyle ConstructorsCodeFreeCheck disable
// @checkstyle ProhibitStaticNestedClassesCheck disable
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
@SuppressWarnings({
    "PMD.LongVariable",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public final class ManagedBean implements BeanNameAware, BeanFactoryAware,
    ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * Зависимость.
     */
    private final Dependency dependency;

    /**
     * Имя бина, полученное от контейнера.
     */
    private String beanName;

    /**
     * Признак того, что фабрика бинов внедрена.
     */
    private boolean beanFactoryInjected;

    /**
     * Признак того, что контекст внедрён.
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

    @Override
    public void setBeanName(final @NonNull String name) {
        this.beanName = name;
        LifecycleLog.record("3-aware-beanName:managedBean");
    }

    @Override
    public void setBeanFactory(final @NonNull BeanFactory beanFactory)
        throws BeansException {
        this.beanFactoryInjected = beanFactory != null;
        LifecycleLog.record("3-aware-beanFactory:managedBean");
    }

    @Override
    public void setApplicationContext(final @NonNull ApplicationContext context)
        throws BeansException {
        this.contextInjected = context != null;
        LifecycleLog.record("3-aware-applicationContext:managedBean");
    }

    /**
     * Шаг 5а: {@code @PostConstruct} вызывается раньше {@code afterPropertiesSet}.
     * @checkstyle NonStaticMethodCheck (4 lines)
     */
    @PostConstruct
    public void postConstruct() {
        LifecycleLog.record("5a-postConstruct:managedBean");
    }

    @Override
    public void afterPropertiesSet() {
        LifecycleLog.record("5b-afterPropertiesSet:managedBean");
    }

    /**
     * Шаг 8а: {@code @PreDestroy} вызывается раньше {@code destroy}.
     * @checkstyle NonStaticMethodCheck (4 lines)
     */
    @PreDestroy
    public void preDestroy() {
        LifecycleLog.record("8a-preDestroy:managedBean");
    }

    @Override
    public void destroy() {
        LifecycleLog.record("8b-destroy:managedBean");
    }

    /**
     * Имя бина, полученное от контейнера.
     * @return Имя бина
     */
    public String beanName() {
        return this.beanName;
    }

    /**
     * Все ли *Aware-интерфейсы отработали.
     * @return Признак того, что бин получил все сведения о контейнере
     */
    public boolean fullyAware() {
        return this.beanName != null && this.beanFactoryInjected && this.contextInjected;
    }

    /**
     * Полезная работа бина.
     * @return Отчёт о работе
     */
    public String work() {
        return String.format("работаю с %s", this.dependency.name());
    }

    /**
     * Простая зависимость, чтобы шаг 2 был не гипотетическим.
     * @since 1.0
     */
    public static final class Dependency {

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
        // @checkstyle NonStaticMethodCheck (3 lines)
        public String name() {
            return "зависимость";
        }
    }
}
