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
 * <p>
 * Три зависимости приходят через конструктор — обязательные, {@code final},
 * подменяемые в тесте без всякого контейнера. {@link Clock} внедряется вместо
 * обращения к {@code Instant.now()}, иначе время стало бы скрытой зависимостью,
 * которую невозможно зафиксировать в тесте.
 * <p>
 * {@code @Transactional} и {@link Audited} работают через один и тот же прокси:
 * обе аннотации сами по себе не значат ничего, поведение им даёт обёртка вокруг бина.
 */
@Service
public final class TaskService implements Tasks {

    private final TaskRepository repository;

    private final TrackerProperties settings;

    private final Clock clock;

    public TaskService(TaskRepository repository, TrackerProperties settings, Clock clock) {
        this.repository = repository;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    @Audited("task.open")
    @Transactional
    public Task open(String title) {
        if (this.repository.findByStatusOrderByCreatedDesc(TaskStatus.OPEN).size()
            >= this.settings.limit()) {
            throw new IllegalStateException(
                "Открытых задач уже " + this.settings.limit() + ", лимит исчерпан"
            );
        }
        return this.repository.save(new Task(title, Instant.now(this.clock)));
    }

    @Override
    @Audited("task.start")
    @Transactional
    public Task start(long id) {
        Task task = task(id);
        task.start();
        return task;
    }

    @Override
    @Audited("task.finish")
    @Transactional
    public Task finish(long id) {
        Task task = task(id);
        task.finish();
        return task;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> byStatus(TaskStatus status) {
        return this.repository.findByStatusOrderByCreatedDesc(status);
    }

    private Task task(long id) {
        return this.repository.findById(id).orElseThrow(
            () -> new IllegalArgumentException("Задачи " + id + " не существует")
        );
    }
}
