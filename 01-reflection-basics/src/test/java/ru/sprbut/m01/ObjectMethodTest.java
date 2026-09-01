/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m01;

import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m01.model.Account;

/**
 * Слайд 8: вызов методов, включая private.
 * @since 1.0
 */
@DisplayName("Слайд 8: вызов методов, включая private")
final class ObjectMethodTest {

    @Test
    @DisplayName("приватный метод вызывается и возвращает результат")
    void invokesPrivateMethod() {
        MatcherAssert.assertThat(
            "private method cannot be invoked reflectively",
            new ObjectMethod(
                new Account("ACC-1", "Иванов", new BigDecimal("100.00")),
                "applyFee",
                BigDecimal.class
            ).call(new BigDecimal("15.00")),
            Matchers.equalTo(new BigDecimal("85.00"))
        );
    }

    @Test
    @DisplayName("побочный эффект приватного метода применяется к объекту")
    void appliesSideEffect() {
        final Account account = new Account("ACC-2", "Иванов", new BigDecimal("100.00"));
        new ObjectMethod(account, "block", String.class).call("подозрение");
        MatcherAssert.assertThat(
            "private void method cannot change the object state",
            account.isBlocked(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("void-метод возвращает null — возвращать ему нечего")
    void returnsNullFromVoid() {
        MatcherAssert.assertThat(
            "void method cannot return null",
            new ObjectMethod(
                new Account("ACC-3", "Иванов", BigDecimal.ONE), "block", String.class
            ).call("причина"),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("исключение из метода приходит развёрнутым, а не как InvocationTargetException")
    void unwrapsRealCause() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new ObjectMethod(
                new Account("ACC-4", "Иванов", null), "applyFee", BigDecimal.class
            ).call(BigDecimal.ONE)
        );
    }

    @Test
    @DisplayName("несуществующий метод даёт понятную ошибку")
    void failsOnUnknownMethod() {
        MatcherAssert.assertThat(
            "unknown method cannot be reported with its own name",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectMethod(
                    new Account("ACC-5", "Иванов", BigDecimal.ONE), "nope"
                ).call()
            ).getMessage(),
            Matchers.containsString("nope")
        );
    }
}
