/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m25.missing;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Слайд «Типичные ошибки»: NoSuchBeanDefinitionException.
 * @since 1.0
 */
@DisplayName("Слайд «Типичные ошибки»: NoSuchBeanDefinitionException")
final class MissingBeanConfigTest {

    @Test
    @DisplayName("контекст без нужного бина не поднимается")
    void dontStartWithoutGateway() {
        MatcherAssert.assertThat(
            "context without a gateway bean cannot fail to start",
            Assertions.assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(MissingBeanConfig.class).close()
            ).getMessage(),
            Matchers.containsString("PaymentGateway")
        );
    }

    @Test
    @DisplayName("падение происходит на старте, а не при первом вызове метода")
    void dontDeferFailureToCallTime() {
        MatcherAssert.assertThat(
            "missing dependency cannot surface before any method call",
            Assertions.assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(MissingBeanConfig.class).close()
            ).getBeanName(),
            Matchers.equalTo(CheckoutService.class.getName())
        );
    }

    @Test
    @DisplayName("один @Bean-метод чинит конфигурацию целиком")
    void repairsContextWithSingleBean() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(RepairedBeanConfig.class)
        ) {
            MatcherAssert.assertThat(
                "declared gateway cannot reach the checkout service",
                context.getBean(CheckoutService.class).pay(),
                Matchers.equalTo("оплата через card")
            );
        }
    }
}
