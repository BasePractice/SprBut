/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

/**
 * Цель замеров: метод настолько дешёвый, что вся измеряемая стоимость
 * приходится на сам механизм вызова, а не на полезную работу.
 * @since 1.0
 */
public class Target {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Target() {
        // нечего инициализировать
    }

    /**
     * Сумма двух чисел.
     * @param first Первый элемент
     * @param second Второй элемент
     * @return Сумма двух чисел
     */
    public int add(final int first, final int second) {
        return first + second;
    }
}
