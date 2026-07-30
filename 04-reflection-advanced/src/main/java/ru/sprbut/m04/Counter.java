package ru.sprbut.m04;

/**
 * Счётчик для демонстрации хэндлов: публичный метод, приватный метод,
 * {@code volatile}-поле для атомарных операций и обычное поле-строка.
 */
@SuppressWarnings("unused")
public class Counter {

    private volatile int value;

    private String label = "счётчик";

    /**
     * Текущее значение.
     */
    public int value() {
        return this.value;
    }

    /**
     * Подпись счётчика.
     */
    public String label() {
        return this.label;
    }

    /**
     * Увеличивает значение и возвращает новое.
     */
    public int increment(int delta) {
        this.value += delta;
        return this.value;
    }

    private String describe(String prefix) {
        return prefix + ": " + this.label + "=" + this.value;
    }
}
