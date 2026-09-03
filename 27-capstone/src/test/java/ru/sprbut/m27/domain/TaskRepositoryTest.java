/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m27.domain;

import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.ApplicationContext;
import ru.sprbut.m27.web.TaskController;

/**
 * Срез @DataJpaTest: только JPA, без веба и аспектов.
 * @since 1.0
 */
@DataJpaTest
@DisplayName("Срез @DataJpaTest: только JPA, без веба и аспектов")
final class TaskRepositoryTest {

    /**
     * Репозиторий.
     */
    @Autowired
    private TaskRepository repository;

    /**
     * Контекст.
     */
    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("сущность получает идентификатор от базы, а не от кода")
    void assignsIdentityOnSave() {
        MatcherAssert.assertThat(
            "database cannot assign the identity on save",
            this.repository.save(
                new Task("проверить срез", Instant.parse("2026-07-30T10:00:00Z"))
            ).id(),
            Matchers.notNullValue()
        );
    }

    @Test
    @DisplayName("запрос выводится из имени метода, реализации в исходниках нет")
    void derivesQueryFromMethodName() {
        this.repository.save(new Task("открытая", Instant.parse("2026-07-30T10:00:00Z")));
        MatcherAssert.assertThat(
            "query derived from the method name cannot find the open task",
            this.repository.findByStatusOrderByCreatedDesc(TaskStatus.OPEN),
            Matchers.hasSize(1)
        );
    }

    @Test
    @DisplayName("сортировка из имени метода отдаёт новые задачи первыми")
    void sortsNewestFirst() {
        this.repository.save(new Task("старая", Instant.parse("2026-07-29T10:00:00Z")));
        this.repository.save(new Task("новая", Instant.parse("2026-07-30T10:00:00Z")));
        MatcherAssert.assertThat(
            "derived ordering cannot put the newest task first",
            this.repository.findByStatusOrderByCreatedDesc(TaskStatus.OPEN)
                .stream().map(Task::title).toList(),
            Matchers.contains("новая", "старая")
        );
    }

    @Test
    @DisplayName("срез не поднимает веб-слой — контроллера в контексте нет")
    void dontLoadWebLayer() {
        MatcherAssert.assertThat(
            "JPA slice cannot leave the controllers out of the context",
            this.context.getBeanNamesForType(TaskController.class).length,
            Matchers.equalTo(0)
        );
    }
}
