/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m03;

import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

/**
 * СХЕМА 1: узел Method.
 * @since 1.0
 */
@DisplayName("СХЕМА 1: узел Method")
final class MethodSignatureTest {

    @Test
    @DisplayName("возвращаемый тип читается напрямую")
    void readsReturnType() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "return type cannot be read from the signature",
            new MethodSignature(Order.class.getMethod("getTotal")).returnType(),
            Matchers.equalTo(BigDecimal.class)
        );
    }

    @Test
    @DisplayName("void — тоже отдельный Class, а не отсутствие типа")
    void treatsVoidAsType() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "void method cannot be recognised by its return type",
            new MethodSignature(Order.class.getMethod("cancel")).voidResult(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("типы параметров перечисляются в порядке объявления")
    void readsParameterTypes() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "parameter types cannot be listed in declaration order",
            new MethodSignature(Order.class.getMethod("setCustomer", String.class)).parameterTypes(),
            Matchers.contains("String")
        );
    }

    @Test
    @DisplayName("имена параметров доступны, потому что модуль собран с флагом -parameters")
    void readsParameterNames() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "parameter names cannot survive compilation",
            new MethodSignature(Order.class.getMethod("setCustomer", String.class)).parameterNames(),
            Matchers.contains("customer")
        );
    }

    @Test
    @DisplayName("объявленные исключения — то, что стоит после throws")
    void readsDeclaredExceptions() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "declared exceptions cannot be read from the signature",
            new MethodSignature(Order.class.getMethod("pay", BigDecimal.class)).exceptions(),
            Matchers.contains("PaymentException")
        );
    }

    @Test
    @DisplayName("varargs распознаётся отдельным флагом")
    void detectsVarargs() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "varargs method cannot be detected by its flag",
            new MethodSignature(Order.class.getMethod("addLines", BigDecimal[].class)).varargs(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("в байткоде varargs — обычный параметр-массив")
    void treatsVarargsAsArray() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "varargs parameter cannot appear as an array",
            new MethodSignature(Order.class.getMethod("addLines", BigDecimal[].class))
                .parameterTypes(),
            Matchers.contains("BigDecimal[]")
        );
    }

    @Test
    @DisplayName("компактная подпись годится для логов и сообщений об ошибках")
    void printsCompactSignature() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "compact signature cannot be printed",
            new MethodSignature(Order.class.getMethod("pay", BigDecimal.class)).text(),
            Matchers.equalTo("void pay(BigDecimal)")
        );
    }
}
