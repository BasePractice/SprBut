package ru.sprbut.m03.extended;

import java.lang.reflect.InvocationTargetException;

/**
 * Настоящая причина отказа рефлексивного вызова.
 * <p>
 * Ошибка разбора команды и ошибка её выполнения — разные вещи, и путать их
 * нельзя: первая означает, что пользователь написал не то, вторая — что метод
 * честно отработал и не согласился.
 */
public final class Unwrapped {

    private final ReflectiveOperationException failure;

    public Unwrapped(ReflectiveOperationException failure) {
        this.failure = failure;
    }

    /**
     * Исключение, которое бросил сам вызванный код.
     */
    public RuntimeException cause() {
        Throwable thrown = this.failure instanceof InvocationTargetException wrapper
            ? wrapper.getCause()
            : this.failure;
        if (thrown instanceof RuntimeException unchecked) {
            return unchecked;
        }
        return new CommandFailed(thrown.getMessage(), thrown);
    }
}
