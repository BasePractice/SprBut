package ru.sprbut.m23.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Хранилище задач.
 * <p>
 * Реализации этого интерфейса не существует ни в исходниках, ни в jar:
 * Spring Data создаёт её в рантайме динамическим прокси, разбирая имена методов.
 * {@code findByStatus} превращается в запрос по одному только названию —
 * это тот же принцип «поведение задаётся метаданными», что и в модуле 01,
 * доведённый до предела.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Задачи в указанном состоянии, новые сверху.
     */
    List<Task> findByStatusOrderByCreatedDesc(TaskStatus status);
}
