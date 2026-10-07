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
 * Тесты {@link BroadcastNotice}.
 * @since 1.0
 */
final class BroadcastNoticeTest {

    @Test
    @DisplayName("общее уведомление доставляется всем как событие broadcast")
    void deliversBroadcastEventToEveryone() {
        final FakeAudience audience = new FakeAudience();
        final String text = UUID.randomUUID().toString();
        new BroadcastNotice(text).deliver(audience);
        MatcherAssert.assertThat(
            "общее уведомление не доставлено как событие broadcast",
            audience.deliveries(),
            Matchers.contains(String.format("all broadcast %s", text))
        );
    }

    @Test
    @DisplayName("общее уведомление пишет в JSON тип BROADCAST")
    void marksTypeInJson() {
        MatcherAssert.assertThat(
            "общее уведомление записало в JSON не тот тип",
            new JsonMapper().readTree(new BroadcastNotice(UUID.randomUUID().toString()).json())
                .get("type").asString(),
            Matchers.equalTo("BROADCAST")
        );
    }
}
