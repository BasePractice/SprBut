/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
// тема раздела — циклические зависимости: участники цикла и три конфигурации
// (сломанная, с @Lazy и переработанная) должны читаться подряд, в отдельных
// файлах сравнение теряется
// @checkstyle ProhibitStaticNestedClassesCheck disable
// @checkstyle QualifyInnerClassCheck disable
// сеттеры участников цикла принимают одноимённое с полем значение —
// именно так их вызывает контейнер
// @checkstyle HiddenFieldCheck disable
package ru.sprbut.m12.cycles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Слайд 94: «Циклические зависимости и {@code @Lazy}».
 *
 * <p>Цикл через <b>конструкторы</b> неразрешим в принципе: чтобы создать A,
 * нужен готовый B, а чтобы создать B — готовый A. Spring это обнаруживает
 * и падает с {@code BeanCurrentlyInCreationException}.</p>
 *
 * <p>{@code @Lazy} разрывает цикл: вместо настоящего бина в конструктор
 * подставляется прокси, а реальный объект достаётся из контейнера при первом
 * обращении к методу — когда оба уже созданы.</p>
 *
 * <p>Важно понимать, что {@code @Lazy} — это <b>обход симптома</b>. Цикл почти
 * всегда означает, что обязанности разложены неудачно, и правильное решение —
 * выделить третий бин.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class CircularBeans {

    private CircularBeans() {
    }

    // --- Неразрешимый цикл через конструкторы --------------------------------

    /**
     * Значение {@code Alpha}.
     * @since 1.0
     */
    public static class Alpha {

        /**
         * Бета-зависимость.
         */
        private final Beta beta;

        /**
         * Основной конструктор.
         * @param beta Бета-зависимость
         */
        public Alpha(final Beta beta) {
            this.beta = beta;
        }

        /**
         * Описание.
         * @return Описание
         */
        public String describe() {
            return String.format("alpha+%s", this.beta.name());
        }
    }

    /**
     * Значение {@code Beta}.
     * @since 1.0
     */
    public static class Beta {

        /**
         * Альфа-зависимость: она никогда не читается — важен сам факт,
         * что контейнер должен её подставить, и цикл на этом замыкается.
         */
        @SuppressWarnings({"PMD.UnusedPrivateField", "UnusedVariable"})
        private final Alpha alpha;

        /**
         * Основной конструктор.
         * @param alpha Альфа-зависимость
         */
        public Beta(final Alpha alpha) {
            this.alpha = alpha;
        }

        /**
         * Имя.
         * @return Имя
         */
        public String name() {
            return "beta";
        }
    }

    /**
     * Конфигурация.
     * @since 1.0
     */
    @Configuration
    public static class BrokenConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public BrokenConfig() {
            // нечего инициализировать
        }

        /**
         * Альфа-зависимость.
         * @param beta Бета-зависимость
         * @return Альфа-зависимость
         */
        @Bean
        public Alpha alpha(final Beta beta) {
            return new Alpha(beta);
        }

        /**
         * Бета-зависимость.
         * @param alpha Альфа-зависимость
         * @return Бета-зависимость
         */
        @Bean
        public Beta beta(final Alpha alpha) {
            return new Beta(alpha);
        }
    }

    // --- Тот же цикл, разорванный @Lazy --------------------------------------

    /**
     * Конфигурация.
     * @since 1.0
     */
    @Configuration
    public static class LazyConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public LazyConfig() {
            // нечего инициализировать
        }

        /**
         * {@code @Lazy} на параметре: сюда придёт прокси, а настоящий {@code Beta}
         * будет получен из контейнера при первом вызове его метода.
         * @param beta Бета-зависимость
         * @return Альфа, собранная на прокси вместо настоящей беты
         */
        @Bean
        public Alpha alpha(@Lazy final Beta beta) {
            return new Alpha(beta);
        }

        /**
         * Бета-зависимость.
         * @param alpha Альфа-зависимость
         * @return Бета-зависимость
         */
        @Bean
        public Beta beta(final Alpha alpha) {
            return new Beta(alpha);
        }
    }

    // --- Цикл через сеттеры: разрешим, но объекты временно невалидны ---------

    /**
     * Значение {@code Gamma}.
     * @since 1.0
     */
    public static class Gamma {

        /**
         * Дельта-зависимость.
         */
        private Delta delta;

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Gamma() {
            // нечего инициализировать
        }

        /**
         * Новое значение: дельта-зависимость.
         * @param delta Дельта-зависимость
         */
        @Autowired
        public void setDelta(final Delta delta) {
            this.delta = delta;
        }

        /**
         * Описание.
         * @return Описание
         */
        public String describe() {
            return String.format("gamma+%s", this.delta.name());
        }
    }

    /**
     * Значение {@code Delta}.
     * @since 1.0
     */
    public static class Delta {

        /**
         * Гамма-зависимость.
         */
        private Gamma gamma;

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public Delta() {
            // нечего инициализировать
        }

        /**
         * Новое значение: гамма-зависимость.
         * @param gamma Гамма-зависимость
         */
        @Autowired
        public void setGamma(final Gamma gamma) {
            this.gamma = gamma;
        }

        /**
         * Имя.
         * @return Имя
         */
        public String name() {
            return "delta";
        }

        /**
         * Знание о гамме.
         * @return Знание о гамме
         */
        public boolean knowsGamma() {
            return this.gamma != null;
        }
    }

    /**
     * Конфигурация.
     * @since 1.0
     */
    @Configuration
    public static class SetterCycleConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public SetterCycleConfig() {
            // нечего инициализировать
        }

        /**
         * Гамма-зависимость.
         * @return Гамма-зависимость
         */
        @Bean
        public Gamma gamma() {
            return new Gamma();
        }

        /**
         * Дельта-зависимость.
         * @return Дельта-зависимость
         */
        @Bean
        public Delta delta() {
            return new Delta();
        }
    }

    // --- Правильное решение: третий бин без цикла ----------------------------

    /**
     * Значение {@code SharedRules}.
     * @since 1.0
     */
    public static class SharedRules {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public SharedRules() {
            // нечего инициализировать
        }

        /**
         * Правило.
         * @return Правило
         */
        public String rule() {
            return "общее правило";
        }
    }

    /**
     * Значение {@code Epsilon}.
     * @since 1.0
     */
    public static class Epsilon {

        /**
         * Правила.
         */
        private final SharedRules rules;

        /**
         * Основной конструктор.
         * @param rules Правила
         */
        public Epsilon(final SharedRules rules) {
            this.rules = rules;
        }

        /**
         * Описание.
         * @return Описание
         */
        public String describe() {
            return String.format("epsilon: %s", this.rules.rule());
        }
    }

    /**
     * Значение {@code Zeta}.
     * @since 1.0
     */
    public static class Zeta {

        /**
         * Правила.
         */
        private final SharedRules rules;

        /**
         * Основной конструктор.
         * @param rules Правила
         */
        public Zeta(final SharedRules rules) {
            this.rules = rules;
        }

        /**
         * Описание.
         * @return Описание
         */
        public String describe() {
            return String.format("zeta: %s", this.rules.rule());
        }
    }

    /**
     * Конфигурация.
     * @since 1.0
     */
    @Configuration
    public static class RefactoredConfig {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public RefactoredConfig() {
            // нечего инициализировать
        }

        /**
         * Общие правила.
         * @return Общие правила
         */
        @Bean
        public SharedRules sharedRules() {
            return new SharedRules();
        }

        /**
         * Значение {@code epsilon}.
         * @param rules Правила
         * @return Значение {@code epsilon}
         */
        @Bean
        public Epsilon epsilon(final SharedRules rules) {
            return new Epsilon(rules);
        }

        /**
         * Зета-зависимость.
         * @param rules Правила
         * @return Зета-зависимость
         */
        @Bean
        public Zeta zeta(final SharedRules rules) {
            return new Zeta(rules);
        }
    }
}
