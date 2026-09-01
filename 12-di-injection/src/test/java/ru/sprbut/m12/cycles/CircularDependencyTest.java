/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m12.cycles;

import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Слайд 94: циклические зависимости и @Lazy.
 * @since 1.0
 */
@DisplayName("Слайд 94: циклические зависимости и @Lazy")
final class CircularDependencyTest {

    @Test
    @DisplayName("Цикл через конструкторы неразрешим — контекст не поднимается")
    void constructorCycleBreaksTheContext() {
        MatcherAssert.assertThat(
            "constructor cycle cannot break the context",
            Assertions.assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(CircularBeans.BrokenConfig.class)
            ).getMessage(),
            Matchers.containsString("Circular")
        );
    }

    @Test
    @DisplayName("@Lazy разрывает цикл: вместо бина подставляется прокси")
    void lazyBreaksTheCycle() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.LazyConfig.class)) {

            final CircularBeans.Alpha alpha = context.getBean(CircularBeans.Alpha.class);
            MatcherAssert.assertThat(
                "lazy proxy cannot break the cycle",
                alpha.describe(),
                Matchers.equalTo("alpha+beta")
            );
        }
    }

    @Test
    @DisplayName("Цикл через сеттеры разрешается, но объекты временно неполны")
    void setterCycleIsResolvable() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.SetterCycleConfig.class)) {

            final CircularBeans.Gamma gamma = context.getBean(CircularBeans.Gamma.class);
            final CircularBeans.Delta delta = context.getBean(CircularBeans.Delta.class);
            MatcherAssert.assertThat(
                "setter cycle cannot be resolved by the container",
                gamma.describe(),
                Matchers.equalTo("gamma+delta")
            );
        }
    }

    @Test
    @DisplayName("Правильное решение — третий бин: цикла нет вовсе")
    void extractingAThirdBeanRemovesTheCycle() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.RefactoredConfig.class)) {

            MatcherAssert.assertThat(
                "extracted third bean cannot remove the cycle",
                context.getBean(CircularBeans.Epsilon.class).describe(),
                Matchers.equalTo("epsilon: общее правило")
            );
        }
    }

    @Test
    @DisplayName("оба бина зависят от общего третьего, и ни один — от другого")
    void sharesTheExtractedBean() {
        try (var context =
                 new AnnotationConfigApplicationContext(CircularBeans.RefactoredConfig.class
)) {
            MatcherAssert.assertThat(
                "shared bean cannot appear in the context",
                Arrays.asList(context.getBeanDefinitionNames()),
                Matchers.hasItem("sharedRules")
            );
        }
    }

    @Test
    @DisplayName("@Lazy — обход симптома: прокси приходит вместо настоящего объекта")
    void lazyInjectsAProxy() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.LazyConfig.class)) {

            final CircularBeans.Alpha alpha = context.getBean(CircularBeans.Alpha.class);
            // сам alpha — обычный бин, но beta внутри него подменена прокси
            MatcherAssert.assertThat(
                "lazy proxy cannot stand in for the real object",
                alpha.describe(),
                Matchers.containsString("beta")
            );
        }
    }
}
