/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 32: стирание типов не абсолютно.
 * @since 1.0
 */
@DisplayName("Слайд 32: стирание типов не абсолютно")
final class GenericTypeTest {

    @Test
    @DisplayName("параметры типа поля сохранены в атрибуте Signature")
    void keepsFieldTypeArguments() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "field type arguments cannot survive erasure",
            new GenericType(Holder.class.getField("names").getGenericType()).arguments(),
            Matchers.contains("java.lang.String")
        );
    }

    @Test
    @DisplayName("вложенный дженерик остаётся вложенным")
    void keepsNestedTypeArguments() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "nested generic cannot stay nested",
            new GenericType(Holder.class.getField("nested").getGenericType()).arguments(),
            Matchers.contains("java.lang.String", "java.util.List<java.lang.Integer>")
        );
    }

    @Test
    @DisplayName("у необобщённого поля параметров типа нет")
    void reportsNoArgumentsForPlainField() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "plain field cannot report an empty argument list",
            new GenericType(Holder.class.getField("plain").getGenericType()).arguments(),
            Matchers.emptyIterable()
        );
    }

    @Test
    @DisplayName("параметризованный тип распознаётся как ParameterizedType")
    void classifiesParameterizedType() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "parameterized type cannot be classified",
            new GenericType(Holder.class.getField("names").getGenericType()).kind(),
            Matchers.equalTo("ParameterizedType")
        );
    }

    @Test
    @DisplayName("wildcard — отдельный род узла")
    void classifiesWildcard() throws NoSuchFieldException {
        final Type argument = ((ParameterizedType) Holder.class.getField("covariant").getGenericType())
            .getActualTypeArguments()[0];
        MatcherAssert.assertThat(
            "wildcard cannot be classified as its own kind",
            new GenericType(argument).kind(),
            Matchers.equalTo("WildcardType")
        );
    }

    @Test
    @DisplayName("переменная типа — тоже отдельный род узла")
    void classifiesTypeVariable() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "type variable cannot be classified as its own kind",
            new GenericType(Holder.class.getField("typeVariable").getGenericType()).kind(),
            Matchers.equalTo("TypeVariable")
        );
    }

    @Test
    @DisplayName("обобщённый массив отличается от обычного")
    void classifiesGenericArray() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "generic array cannot be told apart from a plain one",
            new GenericType(Holder.class.getField("genericArray").getGenericType()).kind(),
            Matchers.equalTo("GenericArrayType")
        );
    }

    @Test
    @DisplayName("верхняя граница wildcard читается напрямую")
    void readsWildcardBound() throws NoSuchFieldException {
        final Type argument = ((ParameterizedType) Holder.class.getField("covariant").getGenericType())
            .getActualTypeArguments()[0];
        MatcherAssert.assertThat(
            "wildcard upper bound cannot be read",
            new GenericType(argument).bounds(),
            Matchers.contains("java.lang.Number")
        );
    }

    @Test
    @DisplayName("границы переменной типа читаются так же")
    void readsTypeVariableBounds() throws NoSuchFieldException {
        MatcherAssert.assertThat(
            "type variable bounds cannot be read",
            new GenericType(Holder.class.getField("typeVariable").getGenericType()).bounds(),
            Matchers.contains("java.lang.Comparable<T>")
        );
    }

    @Test
    @DisplayName("тип возвращаемого значения тоже сохраняет параметры")
    void keepsReturnTypeArguments() throws NoSuchMethodException {
        MatcherAssert.assertThat(
            "return type arguments cannot survive erasure",
            new GenericType(Holder.class.getMethod("produce").getGenericReturnType()).arguments(),
            Matchers.contains("T")
        );
    }

    @Test
    @DisplayName("анонимный подкласс ловит фактический параметр типа")
    void capturesTypeByToken() {
        MatcherAssert.assertThat(
            "type token cannot capture the actual type argument",
            new TypeToken<List<String>>() {
            }.type().getTypeName(),
            Matchers.equalTo("java.util.List<java.lang.String>")
        );
    }
}
