package ru.sprbut.m23.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Задача трекера — сущность JPA.
 * <p>
 * Класс не {@code final}, поля не {@code final}, есть конструктор без аргументов:
 * всё это требования Hibernate, который строит ленивый прокси-подкласс и заполняет
 * поля рефлексией — ровно тем механизмом, с которого начинался курс.
 * <p>
 * Сеттеров при этом нет: состояние меняют {@link #start()} и {@link #finish()},
 * которые знают правила переходов. Открытость Hibernate и открытость для
 * прикладного кода — разные вещи.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(nullable = false)
    private Instant created;

    protected Task() {
    }

    public Task(String title, Instant created) {
        this.title = title;
        this.status = TaskStatus.OPEN;
        this.created = created;
    }

    public Long id() {
        return this.id;
    }

    public String title() {
        return this.title;
    }

    public TaskStatus status() {
        return this.status;
    }

    public Instant created() {
        return this.created;
    }

    /**
     * Переводит задачу в работу.
     */
    public void start() {
        move(TaskStatus.IN_PROGRESS);
    }

    /**
     * Закрывает задачу.
     */
    public void finish() {
        move(TaskStatus.DONE);
    }

    private void move(TaskStatus next) {
        if (!next.reachableFrom(this.status)) {
            throw new IllegalStateException(
                "Задача в состоянии " + this.status + " не переходит в " + next
            );
        }
        this.status = next;
    }
}
