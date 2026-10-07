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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Тесты {@link UserNotice}.
 * @since 1.0
 */
final class UserNoticeTest {

    @Test
    @DisplayName("личное уведомление доставляется своему пользователю как user-event")
    void deliversUserEventToItsUser() {
        final FakeAudience audience = new FakeAudience();
        final long user = ThreadLocalRandom.current().nextLong(1L, 1000L);
        final String text = UUID.randomUUID().toString();
        new UserNotice(user, text).deliver(audience);
        MatcherAssert.assertThat(
            "личное уведомление не доставлено как user-event",
            audience.deliveries(),
            Matchers.contains(String.format("%d user-event %s", user, text))
        );
    }

    @Test
    @DisplayName("личное уведомление пишет пользователя в JSON")
    void writesUserIntoJson() {
        final long user = ThreadLocalRandom.current().nextLong(1L, 1000L);
        MatcherAssert.assertThat(
            "пользователь потерялся в JSON личного уведомления",
            new JsonMapper().readTree(new UserNotice(user, "привет").json())
                .get("userId").asLong(),
            Matchers.equalTo(user)
        );
    }

    @Test
    @DisplayName("личное уведомление экранирует спецсимволы в JSON")
    void escapesQuotesInJson() {
        final String text = String.format("\"%s\"%n{}\\", UUID.randomUUID());
        MatcherAssert.assertThat(
            "спецсимволы в тексте сломали JSON личного уведомления",
            new JsonMapper().readTree(new UserNotice(7L, text).json()).get("text").asString(),
            Matchers.equalTo(text)
        );
    }
}
