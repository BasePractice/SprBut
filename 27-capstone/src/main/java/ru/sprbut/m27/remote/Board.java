/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m27.remote;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import ru.sprbut.m27.audit.AuditTrail;
import ru.sprbut.m27.domain.Task;

/**
 * Доска соседа под предохранителем.
 *
 * <p>Между сервисами у вызова появляется третий исход — «не отвечает».
 * Предохранитель считает неудачи и, перейдя порог, размыкает цепь: следующие
 * вызовы не идут по сети вовсе. Смысл не в вежливости к соседу, а в себе:
 * поток, ждущий ответа от мёртвого сервиса, занят и не обслуживает никого.</p>
 *
 * <p>Запасной ответ — решение предметной области, а не техники. Трекеру
 * достаточно записать в журнал, что доска молчит: закрытие задачи от этого
 * не отменяется, а расхождение видно в {@code /api/introspection/audit}.</p>
 *
 * @since 1.0
 */
@Service
public final class Board {

    /**
     * Фабрика предохранителей.
     */
    private final CircuitBreakerFactory<?, ?> breakers;

    /**
     * Доска по ту сторону сети.
     */
    private final BoardApi api;

    /**
     * Журнал событий.
     */
    private final AuditTrail trail;

    /**
     * Основной конструктор.
     * @param breakers Фабрика предохранителей
     * @param api Доска по ту сторону сети
     * @param trail Журнал событий
     */
    public Board(final CircuitBreakerFactory<?, ?> breakers, final BoardApi api,
        final AuditTrail trail) {
        this.breakers = breakers;
        this.api = api;
        this.trail = trail;
    }

    /**
     * Сообщает соседу о закрытой задаче и записывает исход в журнал.
     * @param task Задача
     */
    public void announce(final Task task) {
        this.trail.record(
            String.format(
                "board:%s",
                this.breakers.create("board").run(
                    () -> this.api.notice(task.title()),
                    failure -> "offline"
                )
            )
        );
    }
}
