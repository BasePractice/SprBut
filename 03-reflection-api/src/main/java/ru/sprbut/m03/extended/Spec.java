/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.util.Arrays;
import java.util.List;

/**
 * Разобранная часть команды: имя и список сырых аргументов.
 *
 * <p>Одинаково разбирает обе половины — {@code Класс(a,b)} и {@code метод(c,d)}:
 * синтаксис у них общий, и различать их до разбора незачем.</p>
 *
 * @since 1.0
 */
public final class Spec {

    /**
     * Исходное значение.
     */
    private final String raw;

    /**
     * Основной конструктор.
     * @param raw Исходное значение
     */
    public Spec(final String raw) {
        this.raw = raw;
    }

    /**
     * Имя до открывающей скобки.
     * @return Имя до открывающей скобки
     */
    public String name() {
        final int open = this.raw.indexOf('(');
        if (open < 0) {
            return this.raw;
        }
        return this.raw.substring(0, open).trim();
    }

    /**
     * Аргументы между скобками, ещё строками.
     * @return Аргументы между скобками, ещё строками
     */
    public List<String> args() {
        final int open = this.raw.indexOf('(');
        if (open < 0) {
            return List.of();
        }
        if (!this.raw.endsWith(")")) {
            throw new IllegalArgumentException("Не закрыта скобка в: " + this.raw);
        }
        final String inside = this.raw.substring(open + 1, this.raw.length() - 1).trim();
        if (inside.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(inside.split(",")).map(String::trim).toList();
    }
}
