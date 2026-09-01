/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m18;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

/**
 * Слайды 158–172 (СХЕМА 11): «run() → события → ApplicationReadyEvent».
 *
 * <p>Событий много, и различаются они тем, <b>что к этому моменту уже готово</b>.
 * Это и есть ответ на вопрос «куда вешать свой код»:
 * <ul>
 * <li>{@code ApplicationStartingEvent} — не готово ничего, даже Environment.
 * Годится только для инициализации логирования;</li>
 * <li>{@code ApplicationEnvironmentPreparedEvent} — Environment есть, контекста нет.
 * Здесь можно добавить свой источник настроек;</li>
 * <li>{@code ApplicationContextInitializedEvent} — контекст создан, но бинов ещё нет;</li>
 * <li>{@code ApplicationPreparedEvent} — определения бинов загружены, сами бины
 * не созданы. Последний момент, когда можно править {@code BeanDefinition};</li>
 * <li>{@code ContextRefreshedEvent} — все синглтоны созданы, контекст поднят;</li>
 * <li>{@code ApplicationStartedEvent} — контекст поднят, но раннеры ещё не отработали;</li>
 * <li>{@code ApplicationReadyEvent} — <b>всё</b> готово, включая раннеры.
 * Именно сюда вешают «приложение запущено».</li>
 * </ul>
 * Ранние события ({@code Starting}, {@code EnvironmentPrepared}) невозможно
 * поймать бином: контекста ещё нет. Их слушатели регистрируются через
 * {@code META-INF/spring.factories} или {@code SpringApplication.addListeners}.</p>
 *
 * @since 1.0
 */
public final class StartupListeners {

    private StartupListeners() {
    }

    /**
     * Самое раннее событие: нет ни Environment, ни контекста.
     * @since 1.0
     */
    public static class Starting implements ApplicationListener<ApplicationStartingEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Starting() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationStartingEvent event) {
            StartupLog.record("1-ApplicationStartingEvent");
        }
    }

    /**
     * Environment готов — можно добавить свой источник настроек.
     * @since 1.0
     */
    public static class EnvironmentPrepared
            implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public EnvironmentPrepared() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationEnvironmentPreparedEvent event) {
            StartupLog.record("2-ApplicationEnvironmentPreparedEvent");
            StartupLog.record("2-env-has-property:"
                    + event.getEnvironment().containsProperty("sprbut.startup.marker"));
        }
    }

    /**
     * Контекст создан, инициализаторы отработали, бинов ещё нет.
     * @since 1.0
     */
    public static class ContextInitialized
            implements ApplicationListener<ApplicationContextInitializedEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public ContextInitialized() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationContextInitializedEvent event) {
            StartupLog.record("4-ApplicationContextInitializedEvent");
        }
    }

    /**
     * Определения бинов загружены — последний шанс их изменить.
     * @since 1.0
     */
    public static class Prepared implements ApplicationListener<ApplicationPreparedEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Prepared() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationPreparedEvent event) {
            StartupLog.record("5-ApplicationPreparedEvent");
        }
    }

    /**
     * refresh() завершён: все синглтоны созданы.
     * @since 1.0
     */
    public static class Refreshed implements ApplicationListener<ContextRefreshedEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Refreshed() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ContextRefreshedEvent event) {
            StartupLog.record("7-ContextRefreshedEvent");
        }
    }

    /**
     * Контекст поднят, но раннеры ещё не выполнялись.
     * @since 1.0
     */
    public static class Started implements ApplicationListener<ApplicationStartedEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Started() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationStartedEvent event) {
            StartupLog.record("8-ApplicationStartedEvent");
        }
    }

    /**
     * Всё готово, включая раннеры. Финал последовательности.
     * @since 1.0
     */
    public static class Ready implements ApplicationListener<ApplicationReadyEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Ready() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationReadyEvent event) {
            StartupLog.record("10-ApplicationReadyEvent");
        }
    }

    /**
     * Запуск не удался — единственное событие, которое отменяет остальные.
     * @since 1.0
     */
    public static class Failed implements ApplicationListener<ApplicationFailedEvent> {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Failed() {
            // нечего инициализировать
        }

        @Override
        public void onApplicationEvent(final @NonNull ApplicationFailedEvent event) {
            StartupLog.record("x-ApplicationFailedEvent:"
                    + event.getException().getClass().getSimpleName());
        }
    }
}
