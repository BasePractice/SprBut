package ru.sprbut.m05.samples;

/**
 * Подкласс, до которого {@code @Marker} не доходит: наследование аннотаций
 * включается самой аннотацией, а не иерархией.
 */
public class MarkedChild extends MarkedParent {
}
