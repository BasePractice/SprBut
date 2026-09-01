/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m03;

import java.math.BigDecimal;
import java.util.AbstractList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

/**
 * СХЕМА 1: узел Constructor.
 * @since 1.0
 */
@DisplayName("СХЕМА 1: узел Constructor")
final class ConstructorsTest {

    @Test
    @DisplayName("конструктор без параметров находится отдельным запросом")
    void findsNoArgConstructor() {
        MatcherAssert.assertThat(
            "no-arg constructor cannot be found",
            new Constructors(Order.class).noArg().isPresent(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("«жадная» стратегия выбирает конструктор с максимумом параметров")
    void picksGreediestConstructor() {
        MatcherAssert.assertThat(
            "greediest strategy cannot pick the widest constructor",
            new Constructors(Order.class).greediest().orElseThrow().getParameterCount(),
            Matchers.equalTo(2)
        );
    }

    @Test
    @DisplayName("объявленных конструкторов не меньше, чем публичных")
    void countsDeclaredConstructors() {
        MatcherAssert.assertThat(
            "declared constructors cannot include the non public ones",
            new Constructors(Order.class).declaredArities(),
            Matchers.hasItem(2)
        );
    }

    @Test
    @DisplayName("подбор учитывает количество и типы аргументов")
    void matchesByArgumentTypes() {
        MatcherAssert.assertThat(
            "constructor cannot be matched by argument types",
            new Constructors(Order.class)
                .matching("A-1", new BigDecimal("100"))
                .orElseThrow()
                .getParameterCount(),
            Matchers.equalTo(2)
        );
    }

    @Test
    @DisplayName("null подходит ссылочному параметру")
    void acceptsNullForReferenceParameter() {
        MatcherAssert.assertThat(
            "null cannot fit a reference parameter",
            new Constructors(Order.class).matching(new Object[]{null}).isPresent(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("аргумент неподходящего типа конструктор не выбирает")
    void dontMatchWrongTypes() {
        MatcherAssert.assertThat(
            "wrong argument type cannot be rejected",
            new Constructors(Order.class).matching(42, 42).isPresent(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("newInstance() создаёт объект — точка входа любого IoC-контейнера")
    void createsInstance() {
        MatcherAssert.assertThat(
            "reflection cannot create the object",
            ((Order) new NewInstance(Order.class, "A-1", new BigDecimal("100")).object()).getId(),
            Matchers.equalTo("A-1")
        );
    }

    @Test
    @DisplayName("у абстрактного типа экземпляра быть не может")
    void dontInstantiateAbstractType() {
        Assertions.assertThrows(
            RuntimeException.class,
            () -> new NewInstance(AbstractList.class).object()
        );
    }

    @Test
    @DisplayName("нет подходящего конструктора — понятное сообщение с аргументами")
    void reportsMissingConstructor() {
        MatcherAssert.assertThat(
            "missing constructor cannot be reported with the arguments",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new NewInstance(Order.class, 1, 2, 3).object()
            ).getMessage(),
            Matchers.containsString("Order")
        );
    }
}
