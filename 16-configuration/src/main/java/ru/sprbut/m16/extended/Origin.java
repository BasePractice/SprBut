package ru.sprbut.m16.extended;

/**
 * Где нашлось значение и что именно там лежит.
 *
 * @param source   имя источника свойств
 * @param value    значение в этом источнике
 * @param priority позиция источника в стеке; меньше — важнее
 */
public record Origin(String source, Object value, int priority) {
}
