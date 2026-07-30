package ru.sprbut.m21.missing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Слайд «Типичные ошибки»: NoSuchBeanDefinitionException")
final class MissingBeanConfigTest {

    @Test
    @DisplayName("контекст без нужного бина не поднимается")
    void dontStartWithoutGateway() {
        assertThat(
            "context without a gateway bean cannot fail to start",
            assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(MissingBeanConfig.class).close()
            ).getMessage(),
            containsString("PaymentGateway")
        );
    }

    @Test
    @DisplayName("падение происходит на старте, а не при первом вызове метода")
    void dontDeferFailureToCallTime() {
        assertThat(
            "missing dependency cannot surface before any method call",
            assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(MissingBeanConfig.class).close()
            ).getBeanName(),
            equalTo(CheckoutService.class.getName())
        );
    }

    @Test
    @DisplayName("один @Bean-метод чинит конфигурацию целиком")
    void repairsContextWithSingleBean() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(RepairedBeanConfig.class)) {
            assertThat(
                "declared gateway cannot reach the checkout service",
                context.getBean(CheckoutService.class).pay(),
                equalTo("оплата через card")
            );
        }
    }
}
