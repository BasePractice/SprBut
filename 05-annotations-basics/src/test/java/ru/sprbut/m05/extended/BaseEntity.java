/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.extended;

/**
 * Родитель с собственным ограничением: его поле обязано проверяться
 * наравне с полями наследника.
 * @since 1.0
 */
@SuppressWarnings("unused")
public class BaseEntity {

    /**
     * Идентификатор.
     */
    @NotBlank
    private final String id;

    protected BaseEntity(final String id) {
        this.id = id;
    }
}
