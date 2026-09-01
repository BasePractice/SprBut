/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.lang.reflect.InvocationTargetException;

/**
 * Настоящая причина отказа рефлексивного вызова.
 *
 * <p>Ошибка разбора команды и ошибка её выполнения — разные вещи, и путать их
 * нельзя: первая означает, что пользователь написал не то, вторая — что метод
 * честно отработал и не согласился.</p>
 *
 * @since 1.0
 */
public final class Unwrapped {

    /**
     * Ошибка.
     */
    private final ReflectiveOperationException failure;

    /**
     * Основной конструктор.
     * @param failure Ошибка
     */
    public Unwrapped(final ReflectiveOperationException failure) {
        this.failure = failure;
    }

    /**
     * Исключение, которое бросил сам вызванный код.
     * @return Исключение, которое бросил сам вызванный код
     */
    public RuntimeException cause() {
        final Throwable thrown;
        if (this.failure instanceof InvocationTargetException wrapper) {
            thrown = wrapper.getCause();
        } else {
            thrown = this.failure;
        }
        final RuntimeException real;
        if (thrown instanceof RuntimeException unchecked) {
            real = unchecked;
        } else {
            real = new CommandFailed(thrown.getMessage(), thrown);
        }
        return real;
    }
}
