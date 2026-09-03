/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m25.circular;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Слайд «Типичные ошибки»: circular reference.
 * @since 1.0
 */
@DisplayName("Слайд «Типичные ошибки»: circular reference")
final class CircularConfigTest {

    @Test
    @DisplayName("взаимная зависимость через конструкторы обрывает сборку контекста")
    void dontResolveConstructorCycle() {
        MatcherAssert.assertThat(
            "constructor cycle cannot be rejected at startup",
            Assertions.assertThrows(
                BeanCreationException.class,
                () -> new AnnotationConfigApplicationContext(CircularConfig.class).close()
            ).getMostSpecificCause(),
            Matchers.instanceOf(BeanCurrentlyInCreationException.class)
        );
    }

    @Test
    @DisplayName("@Lazy подставляет прокси и цикл размыкается")
    void breaksCycleByProxy() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(LazyConfig.class)
        ) {
            MatcherAssert.assertThat(
                "lazy proxy cannot let the ledger reach the invoices",
                ((LedgerService) context.getBean(Ledger.class)).balanced(),
                Matchers.equalTo(true)
            );
        }
    }

    @Test
    @DisplayName("разделение бинов убирает цикл, а не прячет его")
    void breaksCycleBySplitting() {
        try (
            AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(SplitConfig.class)
        ) {
            MatcherAssert.assertThat(
                "split configuration cannot compute the invoice total",
                context.getBean(Invoices.class).total(),
                Matchers.equalTo(300)
            );
        }
    }
}
