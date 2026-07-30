package ru.sprbut.m06.members;

/**
 * Уровень изоляции — enum как допустимый тип элемента аннотации.
 */
public enum Isolation {

    DEFAULT,
    READ_COMMITTED,
    SERIALIZABLE
}
