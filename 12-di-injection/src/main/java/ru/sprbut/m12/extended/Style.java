/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m12.extended;

/**
 * Способ, которым класс получает зависимости.
 *
 * <p>Порядок объявления не случаен: он совпадает с порядком предпочтения
 * со слайдов 91–95, от лучшего к худшему.</p>
 *
 * @since 1.0
 */
public enum Style {

    /**
     * Значение {@code CONSTRUCTOR}.
     */
    CONSTRUCTOR,
    /**
     * Значение {@code SETTER}.
     */
    SETTER,
    /**
     * Значение {@code FIELD}.
     */
    FIELD,
    /**
     * Значение {@code SERVICE_LOCATOR}.
     */
    SERVICE_LOCATOR,
    /**
     * Значение {@code NONE}.
     */
    NONE
}
