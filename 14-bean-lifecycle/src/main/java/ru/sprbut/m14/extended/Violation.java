package ru.sprbut.m14.extended;

/**
 * Нарушение ожидаемого порядка шагов.
 *
 * @param rule   правило, которое нарушено
 * @param detail что именно пошло не так
 */
public record Violation(String rule, String detail) {
}
