/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m25.scopes;

/**
 * Singleton, который держит в себе prototype-бин.
 *
 * <p>Здесь и прячется ошибка со слайда: зависимость внедряется <b>один раз</b>,
 * при создании singleton'а. Дальше «прототип» живёт столько же, сколько его
 * владелец, и {@code prototype} остаётся таковым только на бумаге.</p>
 *
 * @since 1.0
 */
public final class Gate {

    /**
     * Талон, полученный при сборке singleton'а.
     */
    private final Ticket ticket;

    /**
     * Основной конструктор.
     * @param ticket Талон, полученный при сборке
     */
    public Gate(final Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Номер очередного посетителя.
     * @return Номер посетителя
     */
    public int admit() {
        return this.ticket.number();
    }
}
