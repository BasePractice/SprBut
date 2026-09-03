/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.web;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.sprbut.m27.domain.TaskStatus;
import ru.sprbut.m27.service.Tasks;

/**
 * Задачи, отданные потоком, а не списком.
 *
 * <p>{@code Flux} — рецепт, а не блюдо: пока никто не подписался, не выполняется
 * ничего, и метод завершается задолго до появления данных. Spring MVC умеет
 * такой ответ сам, без реактивного стека: {@code ReactiveTypeHandler} подписывается
 * на поток и дописывает элементы в открытый ответ по мере поступления.</p>
 *
 * <p>Реактивным приложение от этого не становится, и класс оставлен здесь именно
 * ради честности: за {@code Flux} стоит блокирующий JDBC, а один блокирующий вызов
 * внутри цепочки сводит весь выигрыш к нулю. Реактивен тут ответ, а не работа
 * с данными — разница между модулями 20 и 21 проходит ровно по этой линии.</p>
 *
 * @since 1.0
 */
@Component
public final class TaskFeed {

    /**
     * Задачи.
     */
    private final Tasks tasks;

    /**
     * Представления.
     */
    private final TaskViews views;

    /**
     * Основной конструктор.
     * @param tasks Задачи
     * @param views Представления
     */
    public TaskFeed(final Tasks tasks, final TaskViews views) {
        this.tasks = tasks;
        this.views = views;
    }

    /**
     * Поток задач в указанном состоянии.
     * @param status Статус
     * @return Поток задач в указанном состоянии
     */
    public Flux<TaskView> stream(final TaskStatus status) {
        return Flux.defer(
            () -> Flux.fromIterable(this.views.views(this.tasks.byStatus(status)))
        );
    }
}
