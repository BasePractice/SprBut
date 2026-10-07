/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Pastor
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m30;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
    @DisplayName("уведомление из JSON доставляет личное уведомление")
    void deliversUserNoticeFromJson() {
        final FakeAudience audience = new FakeAudience();
        final long user = ThreadLocalRandom.current().nextLong(1L, 1000L);
        final String text = UUID.randomUUID().toString();
        new ParsedNotice(new UserNotice(user, text).json(), new JsonMapper()).deliver(audience);
        MatcherAssert.assertThat(
            "личное уведомление из JSON не доставлено",
            audience.deliveries(),
            Matchers.contains(String.format("%d user-event %s", user, text))
        );
    }

    @Test
    @DisplayName("уведомление из JSON доставляет общее уведомление")
    void deliversBroadcastNoticeFromJson() {
        final FakeAudience audience = new FakeAudience();
        final String text = UUID.randomUUID().toString();
        new ParsedNotice(new BroadcastNotice(text).json(), new JsonMapper()).deliver(audience);
        MatcherAssert.assertThat(
            "общее уведомление из JSON не доставлено",
            audience.deliveries(),
            Matchers.contains(String.format("all broadcast %s", text))
        );
    }

    @Test
    @DisplayName("читается общее уведомление прошлой версии с пустым пользователем")
    void readsLegacyBroadcastWithNullUser() {
        final FakeAudience audience = new FakeAudience();
        new ParsedNotice(
            "{\"type\":\"BROADCAST\",\"userId\":null,\"text\":\"старое\"}", new JsonMapper()
        ).deliver(audience);
        MatcherAssert.assertThat(
            "общее уведомление прошлой версии не прочитано",
            audience.deliveries(),
            Matchers.contains("all broadcast старое")
        );
    }

    @Test
    @DisplayName("уведомление из JSON возвращает исходный JSON без изменений")
    void returnsJsonAsIs() {
        final String json = String.format("{\"type\":\"%s\"}", UUID.randomUUID());
        MatcherAssert.assertThat(
            "исходный JSON уведомления изменился",
            new ParsedNotice(json, new JsonMapper()).json(),
            Matchers.equalTo(json)
        );
    }

    @Test
    @DisplayName("уведомление неизвестного типа отвергается с указанием типа")
    void rejectsUnknownType() {
        MatcherAssert.assertThat(
            "неизвестный тип принят молча или не назван в ошибке",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ParsedNotice("{\"type\":\"GHOST\",\"text\":\"x\"}", new JsonMapper())
                    .deliver(new FakeAudience())
            ).getMessage(),
            Matchers.containsString("GHOST")
        );
    }

    @Test
    @DisplayName("битый JSON отвергается исключением Jackson")
    void rejectsBrokenJson() {
        MatcherAssert.assertThat(
            "битый JSON принят молча",
            Assertions.assertThrows(
                JacksonException.class,
                () -> new ParsedNotice("{\"type\":", new JsonMapper()).deliver(new FakeAudience())
            ),
            Matchers.notNullValue()
        );
    }

    @Test
    @DisplayName("личное уведомление без пользователя отвергается")
    void rejectsUserNoticeWithoutUser() {
        MatcherAssert.assertThat(
            "личное уведомление без пользователя доставлено",
            Assertions.assertThrows(
                JacksonException.class,
                () -> new ParsedNotice("{\"type\":\"USER\",\"text\":\"x\"}", new JsonMapper())
                    .deliver(new FakeAudience())
            ),
            Matchers.notNullValue()
        );
    }
}
