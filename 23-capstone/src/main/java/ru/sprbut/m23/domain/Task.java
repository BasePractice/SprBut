/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
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
 *
 * <p>Класс не {@code final}, поля не {@code final}, есть конструктор без аргументов:
 * всё это требования Hibernate, который строит ленивый прокси-подкласс и заполняет
 * поля рефлексией — ровно тем механизмом, с которого начинался курс.</p>
 *
 * <p>Сеттеров при этом нет: состояние меняют {@link #start()} и {@link #finish()},
 * которые знают правила переходов. Открытость Hibernate и открытость для
 * прикладного кода — разные вещи.</p>
 *
 * @since 1.0
 */
@Entity
@Table(name = "tasks")
public class Task {
    protected Task() {
        // тело намеренно пустое
    }

    /**
     * Основной конструктор.
     * @param title Название
     * @param created Момент создания
     */
    public Task(final String title, final Instant created) {
        this.title = title;
        this.status = TaskStatus.OPEN;
        this.created = created;
    }

    /**
     * Идентификатор.
     * @return Идентификатор
     */
    public Long id() {
        return this.id;
    }

    /**
     * Название.
     * @return Название
     */
    public String title() {
        return this.title;
    }

    /**
     * Статус.
     * @return Статус
     */
    public TaskStatus status() {
        return this.status;
    }

    /**
     * Момент создания.
     * @return Момент создания
     */
    public Instant created() {
        return this.created;
    }

    /**
     * Переводит задачу в работу.
     */
    public void start() {
        this.move(TaskStatus.IN_PROGRESS);
    }

    /**
     * Закрывает задачу.
     */
    public void finish() {
        this.move(TaskStatus.DONE);
    }

    /**
     * Идентификатор.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Статус.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    /**
     * Момент создания.
     */
    @Column(nullable = false)
    private Instant created;

    private void move(final TaskStatus next) {
        if (!next.reachableFrom(this.status)) {
            throw new IllegalStateException(
                String.format("Задача в состоянии %s не переходит в %s", this.status, next)
            );
        }
        this.status = next;
    }
}
