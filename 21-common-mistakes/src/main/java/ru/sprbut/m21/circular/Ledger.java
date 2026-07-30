package ru.sprbut.m21.circular;

/**
 * Книга проводок — вторая вершина циклической зависимости.
 */
public interface Ledger {

    /**
     * Количество проводок.
     */
    int entries();
}
