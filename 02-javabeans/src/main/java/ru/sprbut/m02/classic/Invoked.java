/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Результат вызова метода бина с развёрнутым исключением.
 *
 * <p>Та же история, что в модуле 01: рефлексия заворачивает ошибку метода
 * в {@link InvocationTargetException}, и отдавать эту обёртку наружу нельзя.</p>
 *
 * @since 1.0
 */
public final class Invoked {

    /**
     * Метод.
     */
    private final Method method;

    /**
     * Целевой объект.
     */
    private final Object target;

    /**
     * Основной конструктор.
     * @param method Метод
     * @param target Целевой объект
     */
    public Invoked(final Method method, final Object target) {
        this.method = method;
        this.target = target;
    }

    /**
     * Возвращённое методом значение.
     * @param args Аргументы
     * @return Возвращённое методом значение
     */
    // unchecked-исключение цели пробрасывается как есть: подменять его
    // обёрткой значит скрывать настоящую причину от вызывающего кода
    @SuppressWarnings("PMD.PreserveStackTrace")
    public Object value(final Object... args) {
        try {
            return this.method.invoke(this.target, args);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Нет доступа к %s", this.method.getName()), denied
            );
        } catch (final InvocationTargetException wrapped) {
            final Throwable cause = wrapped.getCause();
            if (cause instanceof RuntimeException unchecked) {
                throw unchecked;
            }
            throw new IllegalStateException(cause.getMessage(), cause);
        }
    }
}
