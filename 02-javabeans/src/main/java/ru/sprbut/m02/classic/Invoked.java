package ru.sprbut.m02.classic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Результат вызова метода бина с развёрнутым исключением.
 * <p>
 * Та же история, что в модуле 01: рефлексия заворачивает ошибку метода
 * в {@link InvocationTargetException}, и отдавать эту обёртку наружу нельзя.
 */
public final class Invoked {

    private final Method method;

    private final Object target;

    public Invoked(Method method, Object target) {
        this.method = method;
        this.target = target;
    }

    /**
     * Возвращённое методом значение.
     */
    public Object value(Object... args) {
        try {
            return this.method.invoke(this.target, args);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Нет доступа к " + this.method.getName(), denied);
        } catch (InvocationTargetException wrapped) {
            Throwable cause = wrapped.getCause();
            if (cause instanceof RuntimeException unchecked) {
                throw unchecked;
            }
            throw new IllegalStateException(cause);
        }
    }
}
