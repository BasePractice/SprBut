package ru.sprbut.m12.cycles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Слайд 94: циклические зависимости и @Lazy")
class CircularDependencyTest {

    @Test
    @DisplayName("Цикл через конструкторы неразрешим — контекст не поднимается")
    void constructorCycleBreaksTheContext() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext(CircularBeans.BrokenConfig.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Circular");
    }

    @Test
    @DisplayName("@Lazy разрывает цикл: вместо бина подставляется прокси")
    void lazyBreaksTheCycle() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.LazyConfig.class)) {
            CircularBeans.Alpha alpha = context.getBean(CircularBeans.Alpha.class);

            assertThat(alpha.describe()).isEqualTo("alpha+beta");
        }
    }

    @Test
    @DisplayName("Цикл через сеттеры разрешается, но объекты временно неполны")
    void setterCycleIsResolvable() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.SetterCycleConfig.class)) {
            CircularBeans.Gamma gamma = context.getBean(CircularBeans.Gamma.class);
            CircularBeans.Delta delta = context.getBean(CircularBeans.Delta.class);

            assertThat(gamma.describe()).isEqualTo("gamma+delta");
            assertThat(delta.knowsGamma()).isTrue();
        }
    }

    @Test
    @DisplayName("Правильное решение — третий бин: цикла нет вовсе")
    void extractingAThirdBeanRemovesTheCycle() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.RefactoredConfig.class)) {
            assertThat(context.getBean(CircularBeans.Epsilon.class).describe())
                    .isEqualTo("epsilon: общее правило");
            assertThat(context.getBean(CircularBeans.Zeta.class).describe())
                    .isEqualTo("zeta: общее правило");

            // оба зависят от одного и того же экземпляра — и ни один от другого
            assertThat(context.getBeanDefinitionNames())
                    .contains("sharedRules", "epsilon", "zeta");
        }
    }

    @Test
    @DisplayName("@Lazy — обход симптома: прокси приходит вместо настоящего объекта")
    void lazyInjectsAProxy() {
        try (var context = new AnnotationConfigApplicationContext(CircularBeans.LazyConfig.class)) {
            CircularBeans.Alpha alpha = context.getBean(CircularBeans.Alpha.class);

            // сам alpha — обычный бин, но beta внутри него подменена прокси
            assertThat(alpha).isNotNull();
            assertThat(alpha.describe()).contains("beta");
        }
    }
}
