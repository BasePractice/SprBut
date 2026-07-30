package ru.sprbut.m13.conditional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Слайды 104–106: {@code @Primary}, {@code @Lazy}, {@code @DependsOn},
 * {@code @Conditional}, {@code @Profile}.
 * <p>
 * Все они отвечают на вопросы «создавать ли бин вообще» и «когда именно»:
 * <ul>
 *   <li>{@code @Conditional} — программируемое условие; на нём построена
 *       вся автоконфигурация Spring Boot (модуль 19);</li>
 *   <li>{@code @Profile} — частный случай {@code @Conditional} по активному профилю;</li>
 *   <li>{@code @Lazy} — создать не при старте, а при первом обращении;</li>
 *   <li>{@code @DependsOn} — задать порядок там, где его не видно из графа
 *       зависимостей (например, инициализация схемы БД до кэша).</li>
 * </ul>
 */
@Configuration
public class ConditionalConfig {

    /** Порядок фактического создания бинов — заполняется конструкторами. */
    public static final List<String> CREATED = new ArrayList<>();

    public static void reset() {
        CREATED.clear();
    }

    public static class Marker {
        public Marker(String name) {
            CREATED.add(name);
        }
    }

    /** Своё условие: бин создаётся, только если задано системное свойство. */
    public static class OnPropertyCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return "true".equals(context.getEnvironment().getProperty("sprbut.feature.enabled"));
        }
    }

    @Bean
    @Conditional(OnPropertyCondition.class)
    public Marker featureBean() {
        return new Marker("featureBean");
    }

    @Bean
    @Profile("dev")
    public Marker devOnlyBean() {
        return new Marker("devOnlyBean");
    }

    @Bean
    @Profile("!dev")
    public Marker notDevBean() {
        return new Marker("notDevBean");
    }

    /** Создаётся не при старте, а при первом {@code getBean}. */
    @Bean
    @Lazy
    public Marker lazyBean() {
        return new Marker("lazyBean");
    }

    @Bean
    public Marker schemaInitializer() {
        return new Marker("schemaInitializer");
    }

    /**
     * Зависимости в коде нет, но порядок важен. {@code @DependsOn} — единственный
     * способ его выразить.
     */
    @Bean
    @DependsOn("schemaInitializer")
    public Marker cacheWarmer() {
        return new Marker("cacheWarmer");
    }
}
