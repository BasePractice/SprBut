package ru.sprbut.m12.cycles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд 94: циклические зависимости и @Lazy")
final class CircularDependencyTest {

    @Test
    @DisplayName("Цикл через конструкторы неразрешим — контекст не поднимается")
    void constructorCycleBreaksTheContext() {
        assertThat(
            "constructor cycle cannot break the context",
            assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(CircularBeans.BrokenConfig.class)
            ).getMessage(),
            containsString("Circular")
        );
    }

    @Test
    @DisplayName("@Lazy разрывает цикл: вместо бина подставляется прокси")
    void lazyBreaksTheCycle() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.LazyConfig.class)) {
            CircularBeans.Alpha alpha = context.getBean(CircularBeans.Alpha.class);

            assertThat(
                "lazy proxy cannot break the cycle",
                alpha.describe(),
                equalTo("alpha+beta")
            );
        }
    }

    @Test
    @DisplayName("Цикл через сеттеры разрешается, но объекты временно неполны")
    void setterCycleIsResolvable() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.SetterCycleConfig.class)) {
            CircularBeans.Gamma gamma = context.getBean(CircularBeans.Gamma.class);
            CircularBeans.Delta delta = context.getBean(CircularBeans.Delta.class);

            assertThat(
                "setter cycle cannot be resolved by the container",
                gamma.describe(),
                equalTo("gamma+delta")
            );
        }
    }

    @Test
    @DisplayName("Правильное решение — третий бин: цикла нет вовсе")
    void extractingAThirdBeanRemovesTheCycle() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.RefactoredConfig.class)) {
            assertThat(
                "extracted third bean cannot remove the cycle",
                context.getBean(CircularBeans.Epsilon.class).describe(),
                equalTo("epsilon: общее правило")
            );
        }
    }

    @Test
    @DisplayName("оба бина зависят от общего третьего, и ни один — от другого")
    void sharesTheExtractedBean() {
        try (var context =
                 new AnnotationConfigApplicationContext(CircularBeans.RefactoredConfig.class)) {
            assertThat(
                "shared bean cannot appear in the context",
                java.util.Arrays.asList(context.getBeanDefinitionNames()),
                hasItem("sharedRules")
            );
        }
    }

    @Test
    @DisplayName("@Lazy — обход симптома: прокси приходит вместо настоящего объекта")
    void lazyInjectsAProxy() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.LazyConfig.class)) {
            CircularBeans.Alpha alpha = context.getBean(CircularBeans.Alpha.class);

            // сам alpha — обычный бин, но beta внутри него подменена прокси
            assertThat(
                "lazy proxy cannot stand in for the real object",
                alpha.describe(),
                containsString("beta")
            );
        }
    }
}
