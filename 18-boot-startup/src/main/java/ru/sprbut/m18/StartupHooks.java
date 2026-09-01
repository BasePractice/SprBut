/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m18;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.MapPropertySource;
import java.util.Map;

/**
 * Слайды 161, 170: {@code ApplicationContextInitializer}, {@code ApplicationRunner}
 * и {@code BeanFactoryPostProcessor} — три хука, которые вызываются между событиями.
 *
 * <p>У каждого своё место в последовательности и своя задача:
 * <ul>
 * <li>{@code ApplicationContextInitializer} — контекст создан, но пуст.
 * Здесь регистрируют источники настроек и профили программно;</li>
 * <li>{@code BeanFactoryPostProcessor} — определения бинов загружены, сами бины
 * ещё не созданы. Это <b>не</b> {@code BeanPostProcessor} из модуля 14:
 * тот работает с готовыми объектами, а этот — с их описаниями;</li>
 * <li>{@code ApplicationRunner} / {@code CommandLineRunner} — контекст поднят.
 * Отсюда запускают разовые задачи при старте.</li>
 * </ul></p>
 *
 * @since 1.0
 */
public final class StartupHooks {

    private StartupHooks() {
    }

    /** Шаг 3: контекст создан, бинов нет. */
    public static class MarkerInitializer
            implements ApplicationContextInitializer<GenericApplicationContext> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public MarkerInitializer() {
            // нечего инициализировать
        }

        @Override
        public void initialize(final GenericApplicationContext context) {
            StartupLog.record("3-ApplicationContextInitializer");
            // Программное добавление источника настроек — типичная задача инициализатора
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("initializer",
                            Map.of("sprbut.startup.injected", "да")));
        }
    }

    /**
     * Шаг 6: работает с {@code BeanDefinition}, а не с объектами.
     * Бины ещё не созданы — их можно переопределить.
     * @since 1.0
     */
    public static class DefinitionTweaker implements BeanFactoryPostProcessor {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public DefinitionTweaker() {
            // нечего инициализировать
        }

        @Override
        public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
                throws BeansException {
            StartupLog.record("6-BeanFactoryPostProcessor:определений="
                    + beanFactory.getBeanDefinitionCount());
        }
    }

    /** Шаг 9а: раннер с разобранными аргументами. */
    @Order(1)
    public static class FirstRunner implements ApplicationRunner {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public FirstRunner() {
            // нечего инициализировать
        }

        @Override
        public void run(final ApplicationArguments args) {
            StartupLog.record("9a-ApplicationRunner:опции=" + args.getOptionNames());
        }
    }

    /** Шаг 9б: раннер с сырым массивом аргументов. */
    @Order(2)
    public static class SecondRunner implements CommandLineRunner {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public SecondRunner() {
            // нечего инициализировать
        }

        @Override
        public void run(final String... args) {
            StartupLog.record("9b-CommandLineRunner:аргументов=" + args.length);
        }
    }

    /**
     * Конфигурация.
     */
    @Configuration
    public static class HooksConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public HooksConfig() {
            // нечего инициализировать
        }

        /**
         * Правщик определений бинов.
         * @return Правщик определений бинов
         */
        @Bean
        public static BeanFactoryPostProcessor definitionTweaker() {
            return new DefinitionTweaker();
        }

        /**
         * Первый runner.
         * @return Первый runner
         */
        @Bean
        public FirstRunner firstRunner() {
            return new FirstRunner();
        }

        /**
         * Второй runner.
         * @return Второй runner
         */
        @Bean
        public SecondRunner secondRunner() {
            return new SecondRunner();
        }
    }
}
