/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sprbut.m27.audit.Audited;
import ru.sprbut.m27.config.TrackerProperties;
import ru.sprbut.m27.domain.Task;
import ru.sprbut.m27.domain.TaskRepository;
import ru.sprbut.m27.domain.TaskStatus;
import ru.sprbut.m27.remote.Board;

/**
 * Сервисный слой трекера.
 *
 * <p>Три зависимости приходят через конструктор — обязательные, {@code final},
 * подменяемые в тесте без всякого контейнера. {@link Clock} внедряется вместо
 * обращения к {@code Instant.now()}, иначе время стало бы скрытой зависимостью,
 * которую невозможно зафиксировать в тесте.</p>
 *
 * <p>{@code @Transactional}, {@link Audited} и {@code @PreAuthorize} работают через
 * один и тот же прокси: все три аннотации сами по себе не значат ничего, поведение
 * им даёт обёртка вокруг бина. Проверка прав на методе, а не на адресе, — не прихоть:
 * до закрытия задачи ведёт не только HTTP, а правило на адресе прикрывает лишь путь.</p>
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
     * Доска соседнего сервиса.
     */
    private final Board board;

    /**
     * Основной конструктор.
     * @param repository Репозиторий
     * @param settings Настройки
     * @param clock Часы
     * @param board Доска соседнего сервиса
     */
    public TaskService(final TaskRepository repository, final TrackerProperties settings,
        final Clock clock, final Board board) {
        this.repository = repository;
        this.settings = settings;
        this.clock = clock;
        this.board = board;
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
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Task finish(final long id) {
        final Task task = this.task(id);
        task.finish();
        this.board.announce(task);
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
