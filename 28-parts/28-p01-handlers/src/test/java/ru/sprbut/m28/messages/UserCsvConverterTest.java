/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.messages;

import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;
import ru.sprbut.m28.dto.UserDto;

/**
 * Конвертер пользователя в CSV.
 * @since 1.0
 */
final class UserCsvConverterTest {

    @Test
    @DisplayName("пользователь пишется строкой CSV под заголовком")
    void writesCsv() throws Exception {
        final MockHttpOutputMessage output = new MockHttpOutputMessage();
        new UserCsvConverter().write(
            new UserDto("fedor", "f***@example.org"), new MediaType("text", "csv"), output
        );
        MatcherAssert.assertThat(
            "пользователь записан не в CSV",
            output.getBodyAsString(StandardCharsets.UTF_8).lines().toList(),
            Matchers.contains("username,email", "fedor,f***@example.org")
        );
    }

    @Test
    @DisplayName("читать CSV конвертер не берётся")
    void cannotRead() {
        MatcherAssert.assertThat(
            "конвертер взялся читать CSV",
            new UserCsvConverter().canRead(UserDto.class, new MediaType("text", "csv")),
            Matchers.is(false)
        );
    }
}
