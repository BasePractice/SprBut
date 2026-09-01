/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m06.members;

/**
 * Уровень изоляции — enum как допустимый тип элемента аннотации.
 * @since 1.0
 */
public enum Isolation {

    /**
     * Значение {@code DEFAULT}.
     */
    DEFAULT,
    /**
     * Значение {@code READ_COMMITTED}.
     */
    READ_COMMITTED,
    /**
     * Значение {@code SERIALIZABLE}.
     */
    SERIALIZABLE
}
