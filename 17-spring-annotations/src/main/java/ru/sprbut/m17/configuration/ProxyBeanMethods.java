/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m17.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Слайд 143: {@code @Configuration} — и его главная, почти всегда неочевидная
 * особенность.
 *
 * <p>Класс с {@code @Configuration} <b>сам оборачивается CGLIB-прокси</b>. Благодаря
 * этому прямой вызов одного {@code @Bean}-метода из другого не создаёт новый
 * объект, а возвращает бин из контейнера. Без прокси это был бы обычный вызов
 * метода — и в контейнере оказались бы два разных экземпляра одного «синглтона».</p>
 *
 * <p>{@code proxyBeanMethods = false} отключает прокси. Это быстрее и обязательно
 * для AOT/native (модуль 22), но тогда {@code @Bean}-методы нельзя вызывать
 * друг из друга — зависимости надо передавать параметрами.</p>
 *
 * @since 1.0
 */
public final class ProxyBeanMethods {

    private ProxyBeanMethods() {
    }

    /**
     * Считает, сколько объектов реально создано.
     */
    public static final AtomicInteger INSTANCES = new AtomicInteger();

    /**
     * Сброс состояния.
     */
    public static void reset() {
        INSTANCES.set(0);
    }

    /**
     * Общая зависимость.
     * @since 1.0
     */
    public static class Shared {
        /**
         * Счётчик номеров.
         */
        private final int serial;

        /**
         * Основной конструктор.
         */
        public Shared() {
            this.serial = INSTANCES.incrementAndGet();
        }

        /**
         * Счётчик номеров.
         * @return Счётчик номеров
         */
        public int serial() {
            return this.serial;
        }
    }

    /**
     * Потребитель.
     * @param shared Общая зависимость
     * @return Потребитель
     */
    public record Consumer(Shared shared) {
    }

    /**
     * Полноценная конфигурация: вызовы {@code @Bean}-методов перехватываются.
     * @since 1.0
     */
    @Configuration
    public static class ProxiedConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public ProxiedConfig() {
            // нечего инициализировать
        }

        /**
         * Общая зависимость.
         * @return Общая зависимость
         */
        @Bean
        public Shared shared() {
            return new Shared();
        }

        /**
         * Первый элемент.
         * @return Первый элемент
         */
        @Bean
        public Consumer first() {
            // вызов перехвачен прокси — вернётся бин из контейнера
            return new Consumer(this.shared());
        }

        /**
         * Второй элемент.
         * @return Второй элемент
         */
        @Bean
        public Consumer second() {
            return new Consumer(this.shared());
        }
    }

    /**
     * «Lite»-режим: прокси нет, вызовы методов остаются обычными вызовами.
     * @since 1.0
     */
    @Configuration(proxyBeanMethods = false)
    public static final class LiteConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public LiteConfig() {
            // нечего инициализировать
        }

        /**
         * Общая зависимость.
         * @return Общая зависимость
         */
        @Bean
        public Shared shared() {
            return new Shared();
        }

        /**
         * Первый элемент.
         * @return Первый элемент
         */
        @Bean
        public Consumer first() {
            // обычный вызов метода — создаётся НОВЫЙ объект мимо контейнера
            return new Consumer(this.shared());
        }

        /**
         * Второй элемент.
         * @return Второй элемент
         */
        @Bean
        public Consumer second() {
            return new Consumer(this.shared());
        }
    }

    /**
     * Правильный вариант для lite-режима: зависимость приходит параметром.
     * @since 1.0
     */
    @Configuration(proxyBeanMethods = false)
    public static final class LiteConfigDone {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public LiteConfigDone() {
            // нечего инициализировать
        }

        /**
         * Общая зависимость.
         * @return Общая зависимость
         */
        @Bean
        public Shared shared() {
            return new Shared();
        }

        /**
         * Первый элемент.
         * @param shared Общая зависимость
         * @return Первый элемент
         */
        @Bean
        public Consumer first(final Shared shared) {
            return new Consumer(shared);
        }

        /**
         * Второй элемент.
         * @param shared Общая зависимость
         * @return Второй элемент
         */
        @Bean
        public Consumer second(final Shared shared) {
            return new Consumer(shared);
        }
    }
}
