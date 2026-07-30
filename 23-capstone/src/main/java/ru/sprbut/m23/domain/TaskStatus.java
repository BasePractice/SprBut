package ru.sprbut.m23.domain;

/**
 * Состояние задачи в трекере.
 * <p>
 * Переходы разрешены только вперёд — обратная дорога из {@link #DONE}
 * закрыта, и это правило живёт в самой сущности, а не в сервисе.
 */
public enum TaskStatus {

    OPEN,
    IN_PROGRESS,
    DONE;

    /**
     * Можно ли перевести задачу в это состояние из текущего.
     */
    public boolean reachableFrom(TaskStatus current) {
        return ordinal() > current.ordinal();
    }
}
