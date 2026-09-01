/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m14;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Слайд 118 (СХЕМА 7): восемь шагов жизненного цикла бина.
 * @since 1.0
 */
@DisplayName("Слайд 118 (СХЕМА 7): восемь шагов жизненного цикла бина")
final class BeanLifecycleTest {

    @Test
    @DisplayName("шаги проходятся в порядке, объявленном контрактом контейнера")
    void keepsDeclaredOrder() {
        MatcherAssert.assertThat(
            "lifecycle steps cannot follow the declared order",
            started().of("managedBean"),
            Matchers.contains(
                "1-constructor:managedBean",
                "2-dependencies:managedBean",
                "3-aware-beanName:managedBean",
                "3-aware-beanFactory:managedBean",
                "3-aware-applicationContext:managedBean",
                "4-bpp-before:managedBean",
                "5a-postConstruct:managedBean",
                "5b-afterPropertiesSet:managedBean",
                "6-bpp-after:managedBean",
                "8a-preDestroy:managedBean",
                "8b-destroy:managedBean"
            )
        );
    }

    @Test
    @DisplayName("зависимость создаётся раньше того, кому она нужна")
    void createsDependencyFirst() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "dependency cannot be created before its consumer",
            log.indexOf("0-dependency-created:dependency"),
            Matchers.lessThan(log.indexOf("1-constructor:managedBean"))
        );
    }

    @Test
    @DisplayName("@PostConstruct вызывается раньше afterPropertiesSet")
    void callsPostConstructFirst() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "@PostConstruct cannot run before afterPropertiesSet",
            log.indexOf("5a-postConstruct:managedBean"),
            Matchers.lessThan(log.indexOf("5b-afterPropertiesSet:managedBean"))
        );
    }

    @Test
    @DisplayName("BeanPostProcessor.before идёт до инициализации")
    void runsPostProcessorBeforeInit() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "post processor cannot run before the initialisation",
            log.indexOf("4-bpp-before:managedBean"),
            Matchers.lessThan(log.indexOf("5a-postConstruct:managedBean"))
        );
    }

    @Test
    @DisplayName("BeanPostProcessor.after идёт после инициализации — здесь и появляется прокси")
    void runsPostProcessorAfterInit() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "post processor cannot run after the initialisation",
            log.indexOf("6-bpp-after:managedBean"),
            Matchers.greaterThan(log.indexOf("5b-afterPropertiesSet:managedBean"))
        );
    }

    @Test
    @DisplayName("SmartLifecycle.start ждёт готовности всего контекста")
    void startsLifecycleAfterAllBeans() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "SmartLifecycle cannot wait for the whole context",
            log.indexOf("7-smartLifecycle-start:backgroundWorker"),
            Matchers.greaterThan(log.indexOf("6-bpp-after:managedBean"))
        );
    }

    @Test
    @DisplayName("@PreDestroy вызывается раньше DisposableBean.destroy")
    void callsPreDestroyFirst() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "@PreDestroy cannot run before destroy",
            log.indexOf("8a-preDestroy:managedBean"),
            Matchers.lessThan(log.indexOf("8b-destroy:managedBean"))
        );
    }

    @Test
    @DisplayName("остановка SmartLifecycle предшествует уничтожению бинов")
    void stopsLifecycleBeforeDestroy() {
        final LifecycleLog log = started();
        MatcherAssert.assertThat(
            "SmartLifecycle cannot stop before the beans are destroyed",
            log.indexOf("9-smartLifecycle-stop:backgroundWorker"),
            Matchers.lessThan(log.indexOf("8a-preDestroy:managedBean"))
        );
    }

    @Test
    @DisplayName("BeanPostProcessor видит каждый бин, а не только помеченный")
    void processesEveryBean() {
        MatcherAssert.assertThat(
            "post processor cannot see every bean",
            started().events(),
            Matchers.hasItem("6-bpp-after:auditableBean")
        );
    }

    @Test
    @DisplayName("prototype-бин до фазы уничтожения не доходит вовсе")
    void dontDestroyPrototype() {
        MatcherAssert.assertThat(
            "prototype bean cannot skip the destruction phase",
            started().of("prototypeWithDestroy").contains("8a-preDestroy:prototypeWithDestroy"),
            Matchers.equalTo(false)
        );
    }

    private static LifecycleLog started() {
        final LifecycleLog log = new LifecycleLog();
        log.clear();
        new AnnotationConfigApplicationContext(LifecycleConfig.class).close();
        return log;
    }
}
