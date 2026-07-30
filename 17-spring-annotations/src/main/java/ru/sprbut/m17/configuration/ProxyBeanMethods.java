package ru.sprbut.m17.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Слайд 143: {@code @Configuration} — и его главная, почти всегда неочевидная
 * особенность.
 * <p>
 * Класс с {@code @Configuration} <b>сам оборачивается CGLIB-прокси</b>. Благодаря
 * этому прямой вызов одного {@code @Bean}-метода из другого не создаёт новый
 * объект, а возвращает бин из контейнера. Без прокси это был бы обычный вызов
 * метода — и в контейнере оказались бы два разных экземпляра одного «синглтона».
 * <p>
 * {@code proxyBeanMethods = false} отключает прокси. Это быстрее и обязательно
 * для AOT/native (модуль 22), но тогда {@code @Bean}-методы нельзя вызывать
 * друг из друга — зависимости надо передавать параметрами.
 */
public final class ProxyBeanMethods {

    private ProxyBeanMethods() {
    }

    /** Считает, сколько объектов реально создано. */
    public static final AtomicInteger INSTANCES = new AtomicInteger();

    public static void reset() {
        INSTANCES.set(0);
    }

    public static class Shared {
        private final int serial;

        public Shared() {
            this.serial = INSTANCES.incrementAndGet();
        }

        public int serial() {
            return serial;
        }
    }

    public record Consumer(Shared shared) {
    }

    /** Полноценная конфигурация: вызовы {@code @Bean}-методов перехватываются. */
    @Configuration
    public static class ProxiedConfig {

        @Bean
        public Shared shared() {
            return new Shared();
        }

        @Bean
        public Consumer first() {
            // вызов перехвачен прокси — вернётся бин из контейнера
            return new Consumer(shared());
        }

        @Bean
        public Consumer second() {
            return new Consumer(shared());
        }
    }

    /** «Lite»-режим: прокси нет, вызовы методов остаются обычными вызовами. */
    @Configuration(proxyBeanMethods = false)
    public static class LiteConfig {

        @Bean
        public Shared shared() {
            return new Shared();
        }

        @Bean
        public Consumer first() {
            // обычный вызов метода — создаётся НОВЫЙ объект мимо контейнера
            return new Consumer(shared());
        }

        @Bean
        public Consumer second() {
            return new Consumer(shared());
        }
    }

    /** Правильный вариант для lite-режима: зависимость приходит параметром. */
    @Configuration(proxyBeanMethods = false)
    public static class LiteConfigDone {

        @Bean
        public Shared shared() {
            return new Shared();
        }

        @Bean
        public Consumer first(Shared shared) {
            return new Consumer(shared);
        }

        @Bean
        public Consumer second(Shared shared) {
            return new Consumer(shared);
        }
    }
}
