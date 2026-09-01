/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m23.domain;

/**
 * Состояние задачи в трекере.
 *
 * <p>Переходы разрешены только вперёд — обратная дорога из {@link #DONE}
 * закрыта, и это правило живёт в самой сущности, а не в сервисе.</p>
 *
 * @since 1.0
 */
public enum TaskStatus {

    /**
     * Значение {@code OPEN}.
     */
    OPEN,
    /**
     * Значение {@code IN_PROGRESS}.
     */
    IN_PROGRESS,
    /**
     * Значение {@code DONE}.
     */
    DONE;

    /**
     * Можно ли перевести задачу в это состояние из текущего.
     * @param current Значение {@code current}
     * @return Можно ли перевести задачу в это состояние из текущего
     */
    public boolean reachableFrom(final TaskStatus current) {
        return ordinal() > current.ordinal();
    }
}
