/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m18;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал этапов запуска: все слушатели и хуки пишут сюда, и по итоговому списку
 * восстанавливается вся последовательность со слайда 172 (СХЕМА 11).
 *
 * <p>Хранилище статическое по той же причине, что и в модуле 14, только острее:
 * {@code ApplicationContextInitializer} и ранние слушатели вызываются до того,
 * как контекст вообще существует. Внедрить им зависимость физически некуда.</p>
 *
 * @since 1.0
 */
public final class StartupLog {
    /**
     * Значение {@code EVENTS}.
     */
    private static final List<String> EVENTS = new ArrayList<>();

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public StartupLog() {
        // нечего инициализировать
    }

    /**
     * Записывает этап запуска.
     * @param event Событие
     */
    public static synchronized void record(final String event) {
        EVENTS.add(event);
    }

    /**
     * Все записанные этапы по порядку.
     * @return Все записанные этапы по порядку
     // @checkstyle NonStaticMethodCheck (3 lines)
     */
    public synchronized List<String> events() {
        return List.copyOf(EVENTS);
    }

    /**
     * Очищает журнал перед новым прогоном.
     // @checkstyle NonStaticMethodCheck (3 lines)
     */
    public synchronized void clear() {
        EVENTS.clear();
    }

    /**
     * Порядковый номер этапа — по нему проверяется относительный порядок.
     * @param event Событие
     * @return Порядковый номер этапа — по нему проверяется относительный порядок
     // @checkstyle NonStaticMethodCheck (3 lines)
     */
    public synchronized int indexOf(final String event) {
        return EVENTS.indexOf(event);
    }

    /**
     * Случился ли этап вообще.
     * @param event Событие
     * @return Случился ли этап вообще
     // @checkstyle NonStaticMethodCheck (3 lines)
     */
    public synchronized boolean has(final String event) {
        return EVENTS.contains(event);
    }
}
