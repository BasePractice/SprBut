package ru.sprbut.m03.extended;

/**
 * Ошибка выполнения самой команды — в отличие от ошибки её разбора.
 */
public final class CommandFailed extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommandFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
