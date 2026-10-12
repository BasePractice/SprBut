/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Тесты {@link ParsedNotice}.
 * @since 1.0
 */
final class ParsedNoticeTest {

    @Test
    @DisplayName("уведомление из JSON восстанавливает имя события")
    void readsEventFromJson() {
        final String event = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "имя события не восстановлено из JSON",
            new ParsedNotice(new TextNotice(event, "текст").json(), new JsonMapper())
                .sse().event(),
            Matchers.equalTo(event)
        );
    }

    @Test
    @DisplayName("уведомление из JSON восстанавливает текст")
    void readsTextFromJson() {
        final String text = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "текст не восстановлен из JSON",
            new ParsedNotice(new TextNotice("user-event", text).json(), new JsonMapper())
                .sse().data(),
            Matchers.equalTo(text)
        );
    }

    @Test
    @DisplayName("уведомление из JSON возвращает исходный JSON без изменений")
    void returnsJsonAsIs() {
        final String json = String.format("{\"event\":\"%s\"}", UUID.randomUUID());
        MatcherAssert.assertThat(
            "исходный JSON уведомления изменился",
            new ParsedNotice(json, new JsonMapper()).json(),
            Matchers.equalTo(json)
        );
    }

    @Test
    @DisplayName("битый JSON отвергается исключением Jackson")
    void rejectsBrokenJson() {
        MatcherAssert.assertThat(
            "битый JSON принят молча",
            Assertions.assertThrows(
                JacksonException.class,
                () -> new ParsedNotice("{\"event\":", new JsonMapper()).sse()
            ),
            Matchers.notNullValue()
        );
    }

    @Test
    @DisplayName("уведомление без текста отвергается")
    void rejectsNoticeWithoutText() {
        MatcherAssert.assertThat(
            "уведомление без текста разобрано",
            Assertions.assertThrows(
                JacksonException.class,
                () -> new ParsedNotice("{\"event\":\"user-event\"}", new JsonMapper()).sse()
            ),
            Matchers.notNullValue()
        );
    }

    @Test
    @DisplayName("уведомление без имени события отвергается")
    void rejectsNoticeWithoutEvent() {
        MatcherAssert.assertThat(
            "уведомление без имени события разобрано",
            Assertions.assertThrows(
                JacksonException.class,
                () -> new ParsedNotice("{\"text\":\"x\"}", new JsonMapper()).sse()
            ),
            Matchers.notNullValue()
        );
    }
}
