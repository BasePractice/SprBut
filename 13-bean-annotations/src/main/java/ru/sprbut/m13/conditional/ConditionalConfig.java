/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
// @checkstyle NonStaticMethodCheck disable
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
 *
 * <p>Все они отвечают на вопросы «создавать ли бин вообще» и «когда именно»:
 * <ul>
 * <li>{@code @Conditional} — программируемое условие; на нём построена
 * вся автоконфигурация Spring Boot (модуль 19);</li>
 * <li>{@code @Profile} — частный случай {@code @Conditional} по активному профилю;</li>
 * <li>{@code @Lazy} — создать не при старте, а при первом обращении;</li>
 * <li>{@code @DependsOn} — задать порядок там, где его не видно из графа
 * зависимостей (например, инициализация схемы БД до кэша).</li>
 * </ul></p>
 *
 * @since 1.0
 */
@Configuration
public class ConditionalConfig {

    /**
     * Порядок фактического создания бинов — заполняется конструкторами.
     */
    public static final List<String> CREATED = new ArrayList<>(0);

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public ConditionalConfig() {
        // нечего инициализировать
    }

    /**
     * Создаётся не при старте, а при первом {@code getBean}.
     * @return Создаётся не при старте, а при первом {@code getBean}
     */
    @Bean
    @Lazy
    public Marker lazyBean() {
        return new Marker("lazyBean");
    }

    /**
     * Инициализатор схемы.
     * @return Инициализатор схемы
     */
    @Bean
    public Marker schemaInitializer() {
        return new Marker("schemaInitializer");
    }

    /**
     * Сброс состояния.
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public static void reset() {
        CREATED.clear();
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    @Conditional(OnPropertyCondition.class)
    public Marker featureBean() {
        return new Marker("featureBean");
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    @Profile("dev")
    public Marker devOnlyBean() {
        return new Marker("devOnlyBean");
    }

    /**
     * Объект.
     * @return Объект
     */
    @Bean
    @Profile("!dev")
    public Marker notDevBean() {
        return new Marker("notDevBean");
    }

    /**
     * Зависимости в коде нет, но порядок важен. {@code @DependsOn} — единственный
     * способ его выразить.
     * @return Зависимости в коде нет, но порядок важен. {@code @DependsOn} — единственный способ его выразить
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    @Bean
    @DependsOn("schemaInitializer")
    public Marker cacheWarmer() {
        return new Marker("cacheWarmer");
    }

    /**
     * Значение {@code Marker}.
     * @since 1.0
     */
    public static class Marker {

        /**
         * Основной конструктор.
         * @param name Имя
         */
        public Marker(final String name) {
            CREATED.add(name);
        }
    }

    /**
     * Своё условие: бин создаётся, только если задано системное свойство.
     * @since 1.0
     */
    public static class OnPropertyCondition implements Condition {

        /**
         * Открытый конструктор: экземпляр создаёт контейнер.
         */
        public OnPropertyCondition() {
            // нечего инициализировать
        }

        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            return "true".equals(context.getEnvironment().getProperty("sprbut.feature.enabled"));
        }
    }
}
