/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
package ru.sprbut.m13.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Слайд 101: {@code @Scope (singleton, prototype, request, session)}.
 *
 * <p>Скоуп отвечает на один вопрос: <b>сколько экземпляров бина существует
 * и как долго они живут</b>.
 * <ul>
 * <li><b>singleton</b> (по умолчанию) — один на контейнер, создаётся при старте;</li>
 * <li><b>prototype</b> — новый на каждый запрос из контейнера. Внимание:
 * контейнер <b>не управляет</b> его уничтожением, {@code @PreDestroy}
 * у prototype не вызывается никогда;</li>
 * <li><b>request</b>, <b>session</b> — веб-скоупы, требуют веб-контекста.</li>
 * </ul>
 * Отдельная тема — {@code proxyMode} (слайд 110): prototype внутри singleton
 * без прокси создаётся один раз и дальше ведёт себя как singleton.</p>
 *
 * @since 1.0
 */
@Configuration
public class ScopeConfig {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ScopeConfig() {
        // нечего инициализировать
    }

    /**
     * Считает, сколько раз вообще создавался объект каждого типа.
     */
    public static final AtomicInteger SINGLETON_INSTANCES = new AtomicInteger();
    /**
     * Значение {@code PROTOTYPE_INSTANCES}.
     */
    public static final AtomicInteger PROTOTYPE_INSTANCES = new AtomicInteger();

    /**
     * Значение {@code resetCounters}.
     */
    public static void resetCounters() {
        SINGLETON_INSTANCES.set(0);
        PROTOTYPE_INSTANCES.set(0);
    }

    /**
     * Объект.
     */
    public static class SingletonBean {
        /**
         * Счётчик номеров.
         */
        private final int serial;

        /**
         * Основной конструктор.
         */
        public SingletonBean() {
            this.serial = SINGLETON_INSTANCES.incrementAndGet();
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
     * Объект.
     */
    public static class PrototypeBean {
        /**
         * Счётчик номеров.
         */
        private final int serial;

        /**
         * Основной конструктор.
         */
        public PrototypeBean() {
            this.serial = PROTOTYPE_INSTANCES.incrementAndGet();
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
     * Singleton, внутрь которого внедрён prototype <b>без прокси</b>.
     * Классическая ловушка: зависимость создалась один раз вместе с владельцем
     * и больше не меняется — «prototype» превратился в singleton (слайд 110).
     * @since 1.0
     */
    public static class HolderWithoutProxy {
        /**
         * Прототип.
         */
        private final PrototypeBean prototype;

        /**
         * Основной конструктор.
         * @param prototype Прототип
         */
        public HolderWithoutProxy(final PrototypeBean prototype) {
            this.prototype = prototype;
        }

        /**
         * Счётчик номеров.
         * @return Счётчик номеров
         */
        public int prototypeSerial() {
            return this.prototype.serial();
        }
    }

    /**
     * Тот же случай, но prototype объявлен с {@code proxyMode}.
     * Внутрь попадает прокси, и каждый вызов метода получает свежий экземпляр.
     * @since 1.0
     */
    public static class HolderWithProxy {
        /**
         * Прототип.
         */
        private final ProxiedPrototypeBean prototype;

        /**
         * Основной конструктор.
         * @param prototype Прототип
         */
        public HolderWithProxy(final ProxiedPrototypeBean prototype) {
            this.prototype = prototype;
        }

        /**
         * Счётчик номеров.
         * @return Счётчик номеров
         */
        public int prototypeSerial() {
            return this.prototype.serial();
        }
    }

    /**
     * Объект.
     */
    public static class ProxiedPrototypeBean {
        /**
         * Счётчик номеров.
         */
        private final int serial;

        /**
         * Основной конструктор.
         */
        public ProxiedPrototypeBean() {
            this.serial = PROTOTYPE_INSTANCES.incrementAndGet();
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
     * Объект.
     * @return Объект
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SingletonBean singletonBean() {
        return new SingletonBean();
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrototypeBean prototypeBean() {
        return new PrototypeBean();
    }

    /**
     * {@code proxyMode = TARGET_CLASS} — в место внедрения подставляется
     * CGLIB-прокси, который на каждый вызов метода достаёт новый бин из контейнера.
     * @return {@code proxyMode = TARGET_CLASS} — в место внедрения подставляется CGLIB-прокси, который на каждый вызов метода достаёт новый бин из контейнера
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ProxiedPrototypeBean proxiedPrototypeBean() {
        return new ProxiedPrototypeBean();
    }

    /**
     * Держатель.
     * @param prototype Прототип
     * @return Держатель
     */
    @Bean
    public HolderWithoutProxy holderWithoutProxy(final PrototypeBean prototype) {
        return new HolderWithoutProxy(prototype);
    }

    /**
     * Держатель.
     * @param prototype Прототип
     * @return Держатель
     */
    @Bean
    public HolderWithProxy holderWithProxy(final ProxiedPrototypeBean prototype) {
        return new HolderWithProxy(prototype);
    }
}
