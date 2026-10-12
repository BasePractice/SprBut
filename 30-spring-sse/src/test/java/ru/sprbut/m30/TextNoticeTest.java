/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Тесты {@link TextNotice}.
 * @since 1.0
 */
final class TextNoticeTest {

    @Test
    @DisplayName("уведомление становится событием SSE со своим именем")
    void namesEventAsGiven() {
        final String event = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "событие SSE потеряло имя уведомления",
            new TextNotice(event, "текст").sse().event(),
            Matchers.equalTo(event)
        );
    }

    @Test
    @DisplayName("уведомление становится событием SSE со своим текстом")
    void carriesTextAsData() {
        final String text = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "событие SSE потеряло текст уведомления",
            new TextNotice("user-event", text).sse().data(),
            Matchers.equalTo(text)
        );
    }

    @Test
    @DisplayName("уведомление пишет имя события в JSON")
    void writesEventIntoJson() {
        final String event = UUID.randomUUID().toString();
        MatcherAssert.assertThat(
            "имя события потерялось в JSON уведомления",
            new JsonMapper().readTree(new TextNotice(event, "текст").json())
                .get("event").asString(),
            Matchers.equalTo(event)
        );
    }

    @Test
    @DisplayName("уведомление экранирует спецсимволы в JSON")
    void escapesQuotesInJson() {
        final String text = String.format("\"%s\"%n{}\\", UUID.randomUUID());
        MatcherAssert.assertThat(
            "спецсимволы в тексте сломали JSON уведомления",
            new JsonMapper().readTree(new TextNotice("broadcast", text).json())
                .get("text").asString(),
            Matchers.equalTo(text)
        );
    }
}
