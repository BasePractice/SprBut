/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m02.classic;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m02.modern.CustomerRecord;

/**
 * Слайд 16: java.beans.Introspector — штатная работа с соглашением.
 * @since 1.0
 */
@DisplayName("Слайд 16: java.beans.Introspector — штатная работа с соглашением")
final class IntrospectedTest {

    @Test
    @DisplayName("Introspector сам находит свойства по парам методов")
    void discoversProperties() {
        MatcherAssert.assertThat(
            "introspector cannot discover the bean properties",
            new Introspected(CustomerBean.class).names(),
            Matchers.hasItems("id", "firstName", "lastName", "age", "vip", "fullName")
        );
    }

    @Test
    @DisplayName("служебное свойство class в отчёт не попадает")
    void hidesClassProperty() {
        MatcherAssert.assertThat(
            "service property class cannot be filtered out",
            new Introspected(CustomerBean.class).names(),
            Matchers.not(Matchers.hasItem("class"))
        );
    }

    @Test
    @DisplayName("read-write свойство требует и getter, и setter")
    void splitsReadWrite() {
        MatcherAssert.assertThat(
            "read-write list cannot demand both accessors",
            new Introspected(CustomerBean.class).readWrite(),
            Matchers.not(Matchers.hasItem("fullName"))
        );
    }

    @Test
    @DisplayName("вычисляемое свойство доступно только на чтение")
    void splitsReadOnly() {
        MatcherAssert.assertThat(
            "computed property cannot be listed as read only",
            new Introspected(CustomerBean.class).readOnly(),
            Matchers.contains("fullName")
        );
    }

    @Test
    @DisplayName("тип свойства известен заранее — на этом строится конвертация значений")
    void knowsPropertyType() {
        MatcherAssert.assertThat(
            "property type cannot be known ahead of the value",
            new Introspected(CustomerBean.class).descriptor("age").orElseThrow().getPropertyType(),
            Matchers.equalTo(int.class)
        );
    }

    @Test
    @DisplayName("у record Introspector не видит ни одного свойства")
    void dontIntrospectRecord() {
        MatcherAssert.assertThat(
            "record cannot stay invisible to the introspector",
            new Introspected(CustomerRecord.class).names(),
            Matchers.emptyIterable()
        );
    }
}
