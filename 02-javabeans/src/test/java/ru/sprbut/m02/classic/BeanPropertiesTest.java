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
 * Слайд 14: свойство определяется методами, а не полями.
 * @since 1.0
 */
@DisplayName("Слайд 14: свойство определяется методами, а не полями")
final class BeanPropertiesTest {

    @Test
    @DisplayName("getXxx и isXxx одинаково дают свойство на чтение")
    void collectsReadableProperties() {
        MatcherAssert.assertThat(
            "getter and is-getter cannot both yield a property",
            new BeanProperties(CustomerBean.class).readable(),
            Matchers.hasItems("id", "firstName", "vip")
        );
    }

    @Test
    @DisplayName("getFullName() даёт свойство, которому не соответствует ни одно поле")
    void collectsPropertyWithoutField() {
        MatcherAssert.assertThat(
            "computed getter cannot yield a property of its own",
            new BeanProperties(CustomerBean.class).readable(),
            Matchers.hasItem("fullName")
        );
    }

    @Test
    @DisplayName("свойство без сеттера на запись не попадает")
    void dontListComputedPropertyAsWritable() {
        MatcherAssert.assertThat(
            "computed property cannot stay out of the writable list",
            new BeanProperties(CustomerBean.class).writable(),
            Matchers.not(Matchers.hasItem("fullName"))
        );
    }

    @Test
    @DisplayName("метод чтения находится по имени свойства")
    void findsReader() {
        MatcherAssert.assertThat(
            "reader cannot be found by the property name",
            new BeanProperties(CustomerBean.class).reader("firstName").getName(),
            org.hamcrest.Matchers.equalTo("getFirstName")
        );
    }

    @Test
    @DisplayName("для несуществующего свойства метода чтения нет")
    void dontFindReaderForUnknownProperty() {
        MatcherAssert.assertThat(
            "unknown property cannot yield a missing reader",
            new BeanProperties(CustomerBean.class).reader("salary"),
            Matchers.nullValue()
        );
    }

    @Test
    @DisplayName("у record свойств по соглашению нет — аксессоры названы иначе")
    void dontSeeRecordAccessors() {
        MatcherAssert.assertThat(
            "record accessors cannot stay invisible to the convention",
            new BeanProperties(CustomerRecord.class).readable(),
            Matchers.emptyIterable()
        );
    }
}
