package ru.sprbut.m12.cycles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Слайд 94: «Циклические зависимости и {@code @Lazy}».
 * <p>
 * Цикл через <b>конструкторы</b> неразрешим в принципе: чтобы создать A,
 * нужен готовый B, а чтобы создать B — готовый A. Spring это обнаруживает
 * и падает с {@code BeanCurrentlyInCreationException}.
 * <p>
 * {@code @Lazy} разрывает цикл: вместо настоящего бина в конструктор
 * подставляется прокси, а реальный объект достаётся из контейнера при первом
 * обращении к методу — когда оба уже созданы.
 * <p>
 * Важно понимать, что {@code @Lazy} — это <b>обход симптома</b>. Цикл почти
 * всегда означает, что обязанности разложены неудачно, и правильное решение —
 * выделить третий бин.
 */
public final class CircularBeans {

    private CircularBeans() {
    }

    // --- Неразрешимый цикл через конструкторы --------------------------------

    public static class Alpha {
        private final Beta beta;

        public Alpha(Beta beta) {
            this.beta = beta;
        }

        public String describe() {
            return "alpha+" + beta.name();
        }
    }

    public static class Beta {
        private final Alpha alpha;

        public Beta(Alpha alpha) {
            this.alpha = alpha;
        }

        public String name() {
            return "beta";
        }
    }

    @Configuration
    public static class BrokenConfig {

        @Bean
        public Alpha alpha(Beta beta) {
            return new Alpha(beta);
        }

        @Bean
        public Beta beta(Alpha alpha) {
            return new Beta(alpha);
        }
    }

    // --- Тот же цикл, разорванный @Lazy --------------------------------------

    @Configuration
    public static class LazyConfig {

        /**
         * {@code @Lazy} на параметре: сюда придёт прокси, а настоящий {@code Beta}
         * будет получен из контейнера при первом вызове его метода.
         */
        @Bean
        public Alpha alpha(@Lazy Beta beta) {
            return new Alpha(beta);
        }

        @Bean
        public Beta beta(Alpha alpha) {
            return new Beta(alpha);
        }
    }

    // --- Цикл через сеттеры: разрешим, но объекты временно невалидны ---------

    public static class Gamma {
        private Delta delta;

        @Autowired
        public void setDelta(Delta delta) {
            this.delta = delta;
        }

        public String describe() {
            return "gamma+" + delta.name();
        }
    }

    public static class Delta {
        private Gamma gamma;

        @Autowired
        public void setGamma(Gamma gamma) {
            this.gamma = gamma;
        }

        public String name() {
            return "delta";
        }

        public boolean knowsGamma() {
            return gamma != null;
        }
    }

    @Configuration
    public static class SetterCycleConfig {

        @Bean
        public Gamma gamma() {
            return new Gamma();
        }

        @Bean
        public Delta delta() {
            return new Delta();
        }
    }

    // --- Правильное решение: третий бин без цикла ----------------------------

    public static class SharedRules {
        public String rule() {
            return "общее правило";
        }
    }

    public static class Epsilon {
        private final SharedRules rules;

        public Epsilon(SharedRules rules) {
            this.rules = rules;
        }

        public String describe() {
            return "epsilon: " + rules.rule();
        }
    }

    public static class Zeta {
        private final SharedRules rules;

        public Zeta(SharedRules rules) {
            this.rules = rules;
        }

        public String describe() {
            return "zeta: " + rules.rule();
        }
    }

    @Configuration
    public static class RefactoredConfig {

        @Bean
        public SharedRules sharedRules() {
            return new SharedRules();
        }

        @Bean
        public Epsilon epsilon(SharedRules rules) {
            return new Epsilon(rules);
        }

        @Bean
        public Zeta zeta(SharedRules rules) {
            return new Zeta(rules);
        }
    }
}
