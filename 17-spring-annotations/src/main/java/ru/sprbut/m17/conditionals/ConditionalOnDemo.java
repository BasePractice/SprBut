/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m17.conditionals;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Слайды 148–149: {@code @ConditionalOnProperty} и {@code @ConditionalOnMissingBean}.
 *
 * <p>Это не «ещё две аннотации», а <b>основа всей автоконфигурации</b> Spring Boot
 * (модуль 19):
 * <ul>
 * <li>{@code @ConditionalOnProperty} — включить кусок конфигурации настройкой;</li>
 * <li>{@code @ConditionalOnMissingBean} — «дай значение по умолчанию, но
 * отступи, если пользователь объявил своё». Именно эта аннотация делает
 * возможным правило «свой бин переопределяет автоконфигурацию» (слайд 177).</li>
 * </ul>
 * Порядок важен: условие проверяется в момент разбора конфигурации, поэтому
 * пользовательская конфигурация должна быть обработана <b>раньше</b>
 * автоконфигурации — Boot это гарантирует.</p>
 *
 * @since 1.0
 */
public final class ConditionalOnDemo {

    private ConditionalOnDemo() {
    }

    /**
     * Значение {@code Notifier}.
     * @since 1.0
     */
    public interface Notifier {
        /**
         * Отправка.
         * @param message Сообщение
         * @return Отправка
         */
        String send(String message);
    }

    /**
     * Значение {@code ConsoleNotifier}.
     * @param prefix Префикс
     * @return Значение {@code ConsoleNotifier}
     */
    public record ConsoleNotifier(String prefix) implements Notifier {
        @Override
        public String send(final String message) {
            return String.format("%s: %s", this.prefix, message);
        }
    }

    /**
     * Роль «автоконфигурации»: даёт бин по умолчанию, но уступает пользовательскому.
     * @since 1.0
     */
    @Configuration
    public static class DefaultsConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public DefaultsConfig() {
            // нечего инициализировать
        }

        /**
         * Уведомитель.
         * @return Уведомитель
         */
        @Bean
        @ConditionalOnMissingBean(Notifier.class)
        public Notifier notifier() {
            return new ConsoleNotifier("по умолчанию");
        }

        /**
         * Сборщик метрик.
         * @return Сборщик метрик
         */
        @Bean
        @ConditionalOnProperty(name = "sprbut.metrics.enabled", havingValue = "true")
        public String metricsCollector() {
            return "метрики включены";
        }

        /**
         * {@code matchIfMissing} — включено, пока явно не выключили.
         * @return {@code matchIfMissing} — включено, пока явно не выключили
         */
        @Bean
        @ConditionalOnProperty(            name = "sprbut.audit.enabled", havingValue = "true", matchIfMissing = true
)
        public String auditCollector() {
            return "аудит включён";
        }
    }

    /**
     * Роль пользователя: свой бин, который должен победить.
     * @since 1.0
     */
    @Configuration
    public static class UserConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public UserConfig() {
            // нечего инициализировать
        }

        /**
         * Уведомитель.
         * @return Уведомитель
         */
        @Bean
        public Notifier notifier() {
            return new ConsoleNotifier("пользовательский");
        }
    }
}
