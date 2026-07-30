package ru.sprbut.m05.extended;

/**
 * Одно нарушение: какое поле, что не так и какое было значение.
 * <p>
 * Отвергнутое значение хранится вместе с сообщением намеренно: сообщение
 * без него объясняет правило, но не объясняет, почему оно не выполнено.
 *
 * @param field    имя поля
 * @param message  что именно нарушено
 * @param rejected значение, которое не прошло проверку
 */
public record Violation(String field, String message, Object rejected) {

    @Override
    public String toString() {
        return this.field + ": " + this.message + " (было: " + this.rejected + ")";
    }
}
