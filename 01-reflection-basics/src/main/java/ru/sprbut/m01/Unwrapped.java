/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.InvocationTargetException;

/**
 * Настоящая причина, извлечённая из {@link InvocationTargetException}.
 *
 * <p>Рефлексия всегда заворачивает исключение вызванного метода в свою обёртку.
 * Отдавать её наружу нельзя: вызывающий код ловил бы обёртку вместо той ошибки,
 * которую действительно бросили, и ни один {@code catch} не сработал бы как задумано.</p>
 *
 * @since 1.0
 */
public final class Unwrapped {

    /**
     * Обёртка.
     */
    private final InvocationTargetException wrapper;

    /**
     * Основной конструктор.
     * @param wrapper Обёртка
     */
    public Unwrapped(final InvocationTargetException wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * Исключение, которое на самом деле бросил метод.
     * @return Исключение, которое на самом деле бросил метод
     */
    public RuntimeException cause() {
        final Throwable thrown = this.wrapper.getCause();
        if (thrown instanceof RuntimeException unchecked) {
            return unchecked;
        }
        return new IllegalStateException(thrown);
    }
}
