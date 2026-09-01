/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m04;

/**
 * Реальная цель, вокруг которой строится прокси. Ничего не знает ни о прокси,
 * ни о логировании — в этом и смысл: класс не меняется.
 * @since 1.0
 */
public final class SimpleGreeter implements Greeter {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public SimpleGreeter() {
        // нечего инициализировать
    }

    @Override
    public String greet(final String name) {
        return String.format("Привет, %s", name);
    }

    @Override
    public int length(final String text) {
        return text.length();
    }
}
