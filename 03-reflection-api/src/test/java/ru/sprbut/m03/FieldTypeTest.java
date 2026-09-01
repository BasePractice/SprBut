/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m03;

import java.math.BigDecimal;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m03.model.Order;

/**
 * СХЕМА 1: узел Field.
 * @since 1.0
 */
@DisplayName("СХЕМА 1: узел Field")
final class FieldTypeTest {

    @Test
    @DisplayName("getType() отдаёт сырой тип после стирания дженериков")
    void erasesRawType() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "raw type cannot be erased to the bare interface",
            new FieldType(Order.class.getDeclaredField("items")).raw(),
            Matchers.equalTo(List.class)
        );
    }

    @Test
    @DisplayName("getGenericType() сохраняет параметры типа — они лежат в атрибуте Signature")
    void keepsGenericParameters() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "generic type cannot keep its parameters",
            new FieldType(Order.class.getDeclaredField("items")).generic().getTypeName(),
            Matchers.containsString("java.lang.String")
        );
    }

    @Test
    @DisplayName("аргументы дженерика извлекаются по одному — так работает разбор типов в Jackson")
    void extractsTypeArguments() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "type arguments cannot be extracted one by one",
            new FieldType(Order.class.getDeclaredField("discounts")).arguments(),
            Matchers.contains("String", "BigDecimal")
        );
    }

    @Test
    @DisplayName("у необобщённого поля список аргументов типа пуст")
    void reportsNoArgumentsForPlainField() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "plain field cannot report an empty argument list",
            new FieldType(Order.class.getDeclaredField("customer")).arguments(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("getDeclaringClass() указывает, где поле объявлено на самом деле")
    void knowsItsOwner() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "field cannot name its declaring class",
            new FieldType(Order.class.getDeclaredField("total")).owner(),
            Matchers.equalTo(Order.class)
        );
    }

    @Test
    @DisplayName("примитивное поле отличается от ссылочного")
    void detectsPrimitiveField() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "primitive field cannot be told apart from a reference one",
            new FieldType(Order.class.getDeclaredField("paid")).primitive(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("ссылочное поле примитивным не считается")
    void dontCallReferenceFieldPrimitive() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "reference field cannot avoid the primitive flag",
            new FieldType(Order.class.getDeclaredField("total")).primitive(),
            Matchers.equalTo(false)
        );
    }

    @Test
    @DisplayName("у примитива есть тип-обёртка — без неё проверка типов аргументов всегда ложна")
    void boxesPrimitive() {
        MatcherAssert.assertThat(
            "primitive cannot be boxed to its wrapper",
            new Boxed(boolean.class).type(),
            Matchers.equalTo(Boolean.class)
        );
    }

    @Test
    @DisplayName("ссылочный тип остаётся собой")
    void keepsReferenceTypeAsIs() {
        MatcherAssert.assertThat(
            "reference type cannot survive boxing untouched",
            new Boxed(BigDecimal.class).type(),
            Matchers.equalTo(BigDecimal.class)
        );
    }
}
