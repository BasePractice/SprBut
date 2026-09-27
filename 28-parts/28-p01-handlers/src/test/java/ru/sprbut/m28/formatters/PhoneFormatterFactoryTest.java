/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.formatters;

import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m28.dto.Phone;
import ru.sprbut.m28.web.UserController;

/**
 * Фабрика форматтеров телефона по аннотации.
 * @since 1.0
 */
final class PhoneFormatterFactoryTest {

    @Test
    @DisplayName("фабрика берётся только за тип телефона")
    void servesPhoneOnly() {
        MatcherAssert.assertThat(
            "фабрика берётся не за тот тип",
            new PhoneFormatterFactory().getFieldTypes(),
            Matchers.contains(Phone.class)
        );
    }

    @Test
    @DisplayName("форматтер собирается под страну из аннотации на параметре")
    void readsCountryFromAnnotation() throws Exception {
        MatcherAssert.assertThat(
            "форматтер собран без учёта аннотации",
            new PhoneFormatterFactory().getParser(
                UserController.class.getMethod("phone", Phone.class).getParameters()[0]
                    .getAnnotation(PhoneNumber.class),
                Phone.class
            ).parse("8 912 345 67 89", Locale.ROOT),
            Matchers.is(new Phone("79123456789"))
        );
    }
}
