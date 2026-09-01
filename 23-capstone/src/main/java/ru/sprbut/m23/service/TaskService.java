/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sprbut.m23.audit.Audited;
import ru.sprbut.m23.config.TrackerProperties;
import ru.sprbut.m23.domain.Task;
import ru.sprbut.m23.domain.TaskRepository;
import ru.sprbut.m23.domain.TaskStatus;

/**
 * Сервисный слой трекера.
 *
 * <p>Три зависимости приходят через конструктор — обязательные, {@code final},
 * подменяемые в тесте без всякого контейнера. {@link Clock} внедряется вместо
 * обращения к {@code Instant.now()}, иначе время стало бы скрытой зависимостью,
 * которую невозможно зафиксировать в тесте.</p>
 *
 * <p>{@code @Transactional} и {@link Audited} работают через один и тот же прокси:
 * обе аннотации сами по себе не значат ничего, поведение им даёт обёртка вокруг бина.</p>
 *
 * @since 1.0
 */
@Service
public final class TaskService implements Tasks {

    /**
     * Репозиторий.
     */
    private final TaskRepository repository;

    /**
     * Настройки.
     */
    private final TrackerProperties settings;

    /**
     * Часы.
     */
    private final Clock clock;

    /**
     * Основной конструктор.
     * @param repository Репозиторий
     * @param settings Настройки
     * @param clock Часы
     */
    public TaskService(final TaskRepository repository, final TrackerProperties settings,
        final Clock clock) {
        this.repository = repository;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    @Audited("task.open")
    @Transactional
    public Task open(final String title) {
        if (this.repository.findByStatusOrderByCreatedDesc(TaskStatus.OPEN).size()
            >= this.settings.limit()) {
            throw new IllegalStateException(
                String.format("Открытых задач уже %s, лимит исчерпан", this.settings.limit())
            );
        }
        return this.repository.save(new Task(title, Instant.now(this.clock)));
    }

    @Override
    @Audited("task.start")
    @Transactional
    public Task start(final long id) {
        final Task task = this.task(id);
        task.start();
        return task;
    }

    @Override
    @Audited("task.finish")
    @Transactional
    public Task finish(final long id) {
        final Task task = this.task(id);
        task.finish();
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> byStatus(final TaskStatus status) {
        return this.repository.findByStatusOrderByCreatedDesc(status);
    }

    private Task task(final long id) {
        return this.repository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(
                String.format("Задачи %s не существует", id)
            )
        );
    }
}
