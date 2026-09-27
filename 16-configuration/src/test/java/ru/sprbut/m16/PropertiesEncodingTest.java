/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m16;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Слайд 131: у файла свойств есть кодировка, и она не подразумевается.
 * @since 1.0
 */
@DisplayName("Слайд 131: у файла свойств есть кодировка")
final class PropertiesEncodingTest {

    @Test
    @DisplayName("прочитанный через Reader в UTF-8 файл отдаёт русский текст целиком")
    void readsRussianThroughReader() throws IOException {
        final Properties props = new Properties();
        try (
            Reader reader = new InputStreamReader(
                PropertiesEncodingTest.resource(), StandardCharsets.UTF_8
            )
        ) {
            props.load(reader);
        }
        MatcherAssert.assertThat(
            "русский текст не прочитан из файла свойств",
            props.getProperty("text"),
            Matchers.is("Привет медвед!")
        );
    }

    @Test
    @DisplayName("тот же файл, прочитанный потоком байтов, приходит испорченным")
    void spoilsRussianReadAsBytes() throws IOException {
        final Properties props = new Properties();
        try (InputStream stream = PropertiesEncodingTest.resource()) {
            props.load(stream);
        }
        MatcherAssert.assertThat(
            "чтение байтами почему-то сохранило кириллицу",
            props.getProperty("text"),
            Matchers.not("Привет медвед!")
        );
    }

    private static InputStream resource() {
        return Objects.requireNonNull(
            PropertiesEncodingTest.class.getResourceAsStream("/russian.properties")
        );
    }
}
