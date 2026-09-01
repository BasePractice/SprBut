/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m02.classic;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Чтение и запись свойств по строковому имени.
 * @since 1.0
 */
@DisplayName("Чтение и запись свойств по строковому имени")
final class BeanValueTest {

    private static CustomerBean customer() {
        final CustomerBean bean = new CustomerBean();
        bean.setId("C-1");
        bean.setFirstName("Иван");
        bean.setLastName("Петров");
        bean.setAge(33);
        bean.setVip(true);
        return bean;
    }

    @Test
    @DisplayName("свойство читается по имени, без знания класса при компиляции")
    void readsByName() {
        MatcherAssert.assertThat(
            "property cannot be read by its name",
            new BeanValue(customer(), "firstName").value(),
            Matchers.equalTo("Иван")
        );
    }

    @Test
    @DisplayName("boolean-свойство читается через is-getter")
    void readsBooleanProperty() {
        MatcherAssert.assertThat(
            "boolean property cannot be read through its is-getter",
            new BeanValue(customer(), "vip").value(),
            Matchers.equalTo(true)
        );
    }

    @Test
    @DisplayName("свойство пишется по имени — так контейнер заполняет бин из yaml")
    void writesByName() {
        final CustomerBean bean = customer();
        new BeanValue(bean, "firstName").assign("Пётр");
        MatcherAssert.assertThat(
            "property cannot be written by its name",
            bean.getFirstName(),
            Matchers.equalTo("Пётр")
        );
    }

    @Test
    @DisplayName("запись в свойство только для чтения отклоняется с внятной ошибкой")
    void dontWriteReadOnlyProperty() {
        MatcherAssert.assertThat(
            "read only property cannot reject the write",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new BeanValue(customer(), "fullName").assign("X")
            ).getMessage(),
            Matchers.containsString("только на чтение")
        );
    }

    @Test
    @DisplayName("неизвестное свойство отклоняется")
    void dontReadUnknownProperty() {
        MatcherAssert.assertThat(
            "unknown property cannot be reported by its name",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new BeanValue(customer(), "salary").value()
            ).getMessage(),
            Matchers.containsString("salary")
        );
    }

    @Test
    @DisplayName("бин целиком превращается в карту свойств")
    void convertsBeanToMap() {
        MatcherAssert.assertThat(
            "bean cannot be turned into a property map",
            new BeanMap(customer()).values(),
            Matchers.hasEntry("lastName", "Петров")
        );
    }

    @Test
    @DisplayName("слайд 18: пустой бин заведомо невалиден — все свойства пусты")
    void createsInvalidEmptyBean() {
        MatcherAssert.assertThat(
            "empty bean cannot start with null properties",
            ((CustomerBean) new EmptyBean(CustomerBean.class).instance()).getFirstName(),
            Matchers.nullValue()
        );
    }
}
