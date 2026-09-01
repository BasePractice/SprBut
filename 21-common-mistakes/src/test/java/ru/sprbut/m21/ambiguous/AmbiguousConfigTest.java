/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m21.ambiguous;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Слайд «Типичные ошибки»: NoUniqueBeanDefinitionException.
 * @since 1.0
 */
@DisplayName("Слайд «Типичные ошибки»: NoUniqueBeanDefinitionException")
final class AmbiguousConfigTest {

    @Test
    @DisplayName("два кандидата на одну точку внедрения останавливают контекст")
    void dontGuessBetweenTwoCandidates() {
        MatcherAssert.assertThat(
            "ambiguous injection point cannot stop the context",
            Assertions.assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(AmbiguousConfig.class).close()
            ).getMostSpecificCause(),
            Matchers.instanceOf(NoUniqueBeanDefinitionException.class)
        );
    }

    @Test
    @DisplayName("контейнер называет обоих кандидатов поимённо")
    void namesBothCandidates() {
        MatcherAssert.assertThat(
            "ambiguity report cannot list both bean names",
            ((NoUniqueBeanDefinitionException) Assertions.assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(AmbiguousConfig.class).close()
            ).getMostSpecificCause()).getBeanNamesFound(),
            Matchers.containsInAnyOrder("express", "economy")
        );
    }

    @Test
    @DisplayName("@Primary выбирает реализацию по умолчанию за все точки внедрения")
    void resolvesByPrimary() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(PrimaryConfig.class)
        ) {
            MatcherAssert.assertThat(
                "primary shipper cannot win the default injection point",
                context.getBean(DeliveryService.class).promise(),
                Matchers.equalTo(1)
            );
        }
    }

    @Test
    @DisplayName("@Qualifier выбирает реализацию на стороне потребителя")
    void resolvesByQualifier() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(QualifierConfig.class)
        ) {
            MatcherAssert.assertThat(
                "qualified injection point cannot pick the economy shipper",
                context.getBean(EconomyDelivery.class).promise(),
                Matchers.equalTo(7)
            );
        }
    }
}
