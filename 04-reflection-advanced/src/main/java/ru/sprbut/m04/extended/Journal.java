/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04.extended;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал того, что делали аспекты.
 *
 * <p>Существует ради проверяемости: без него единственным свидетельством работы
 * аспекта было бы время выполнения, а на нём тесты писать нельзя.
 * Синхронизация не украшение — один прокси может обслуживать несколько потоков.</p>
 *
 * @since 1.0
 */
public final class Journal {

    /**
     * Записи.
     */
    private final List<String> entries;

    /**
     * Основной конструктор.
     */
    public Journal() {
        this(new ArrayList<>());
    }

    /**
     * Основной конструктор.
     * @param entries Записи
     */
    public Journal(final List<String> entries) {
        this.entries = entries;
    }

    /**
     * Добавляет запись.
     * @param entry Значение {@code entry}
     */
    public synchronized void record(final String entry) {

        this.entries.add(entry);
    }

    /**
     * Снимок журнала.
     * @return Снимок журнала
     */
    public synchronized List<String> entries() {
        return List.copyOf(this.entries);
    }

    /**
     * Сколько записей начинается с указанного префикса.
     * @param prefix Префикс
     * @return Сколько записей начинается с указанного префикса
     */
    public synchronized long count(final String prefix) {
        return this.entries.stream().filter(entry -> entry.startsWith(prefix)).count();
    }
}
