/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.web;

import java.time.Instant;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import ru.sprbut.m27.domain.Task;
import ru.sprbut.m27.domain.TaskStatus;
import ru.sprbut.m27.service.Tasks;

/**
 * Поток вместо списка: Flux в сервлетном приложении.
 * @since 1.0
 */
@DisplayName("Поток вместо списка: Flux в сервлетном приложении")
final class TaskFeedTest {

    @Test
    @DisplayName("подписчик получает задачи по одной, а не списком целиком")
    void deliversTasksOneByOne() {
        final Tasks tasks = Mockito.mock(Tasks.class);
        BDDMockito.given(tasks.byStatus(TaskStatus.OPEN)).willReturn(
            List.of(
                new Task("собрать поток", Instant.parse("2026-07-30T10:00:00Z")),
                new Task("подписаться", Instant.parse("2026-07-30T11:00:00Z"))
            )
        );
        StepVerifier.create(new TaskFeed(tasks, new TaskViewsImpl()).stream(TaskStatus.OPEN))
            .expectNextCount(2L)
            .verifyComplete();
    }

    @Test
    @DisplayName("до подписки не выполняется ничего: Flux — рецепт, а не блюдо")
    void dontTouchTheServiceBeforeSubscription() {
        final Tasks tasks = Mockito.mock(Tasks.class);
        new TaskFeed(tasks, new TaskViewsImpl()).stream(TaskStatus.OPEN);
        MatcherAssert.assertThat(
            "unsubscribed flux cannot leave the service untouched",
            Mockito.mockingDetails(tasks).getInvocations(),
            Matchers.empty()
        );
    }
}
