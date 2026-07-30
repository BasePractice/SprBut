package ru.sprbut.m01;

import java.lang.reflect.InvocationTargetException;

/**
 * Настоящая причина, извлечённая из {@link InvocationTargetException}.
 * <p>
 * Рефлексия всегда заворачивает исключение вызванного метода в свою обёртку.
 * Отдавать её наружу нельзя: вызывающий код ловил бы обёртку вместо той ошибки,
 * которую действительно бросили, и ни один {@code catch} не сработал бы как задумано.
 */
public final class Unwrapped {

    private final InvocationTargetException wrapper;

    public Unwrapped(InvocationTargetException wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * Исключение, которое на самом деле бросил метод.
     */
    public RuntimeException cause() {
        Throwable thrown = this.wrapper.getCause();
        if (thrown instanceof RuntimeException unchecked) {
            return unchecked;
        }
        return new IllegalStateException(thrown);
    }
}
