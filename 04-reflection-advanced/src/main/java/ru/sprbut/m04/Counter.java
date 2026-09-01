/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Счётчик для демонстрации хэндлов: публичный метод, приватный метод,
 * {@code volatile}-поле для атомарных операций и обычное поле-строка.
 * @since 1.0
 */
@SuppressWarnings("unused")
public class Counter {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Counter() {
        // нечего инициализировать
    }

    /**
     * Значение.
     */
    private volatile int value;

    /**
     * Метка.
     */
    private String label = "счётчик";

    /**
     * Текущее значение.
     * @return Текущее значение
     */
    public int value() {
        return this.value;
    }

    /**
     * Подпись счётчика.
     * @return Подпись счётчика
     */
    public String label() {
        return this.label;
    }

    /**
     * Увеличивает значение и возвращает новое.
     * @param delta Дельта-зависимость
     * @return Увеличивает значение и возвращает новое
     */
    public int increment(final int delta) {
        this.value += delta;
        return this.value;
    }

    private String describe(final String prefix) {
        return prefix + ": " + this.label + "=" + this.value;
    }
}
