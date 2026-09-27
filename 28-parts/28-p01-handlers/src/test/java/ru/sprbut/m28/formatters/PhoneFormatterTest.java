/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.formatters;

import java.text.ParseException;
import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sprbut.m28.dto.Phone;

/**
 * Форматтер телефона.
 * @since 1.0
 */
final class PhoneFormatterTest {

    @Test
    @DisplayName("ведущая восьмёрка заменяется кодом страны")
    void replacesLeadingEight() throws Exception {
        MatcherAssert.assertThat(
            "восьмёрка осталась вместо кода страны",
            new PhoneFormatter("7").parse("8 (912) 345-67-89", Locale.ROOT),
            Matchers.is(new Phone("79123456789"))
        );
    }

    @Test
    @DisplayName("к номеру из десяти цифр дописывается код страны")
    void prependsCountry() throws Exception {
        MatcherAssert.assertThat(
            "код страны не дописан к номеру",
            new PhoneFormatter("375").parse("291 234-56-78", Locale.ROOT),
            Matchers.is(new Phone("3752912345678"))
        );
    }

    @Test
    @DisplayName("номер неправильной длины отвергается")
    void rejectsShortNumber() {
        Assertions.assertThrows(
            ParseException.class,
            () -> new PhoneFormatter("7").parse("12-34", Locale.ROOT),
            "короткий номер принят за телефон"
        );
    }

    @Test
    @DisplayName("телефон печатается в международном виде")
    void printsInternational() {
        MatcherAssert.assertThat(
            "телефон напечатан не в международном виде",
            new PhoneFormatter("7").print(new Phone("79123456789"), Locale.ROOT),
            Matchers.is("+79123456789")
        );
    }

    @Test
    @DisplayName("без кода страны форматтер не собирается")
    void rejectsMissingCountry() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new PhoneFormatter(null),
            "форматтер собран без кода страны"
        );
    }
}
