/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// журнал общий для всех этапов запуска: только так виден их порядок
// @checkstyle NonStaticMethodCheck disable
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
@SuppressWarnings({"PMD.ProhibitPublicStaticMethods", "PMD.AvoidSynchronizedStatement"})
public final class StartupLog {

    /**
     * Записанные этапы запуска.
     */
    private static final List<String> EVENTS = new ArrayList<>(0);

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public StartupLog() {
        // нечего инициализировать
    }

    /**
     * Записывает этап запуска.
     * @param event Событие
     */
    public static void record(final String event) {
        synchronized (StartupLog.EVENTS) {
            StartupLog.EVENTS.add(event);
        }
    }

    /**
     * Все записанные этапы по порядку.
     * @return Все записанные этапы по порядку
     */
    public List<String> events() {
        synchronized (StartupLog.EVENTS) {
            return List.copyOf(StartupLog.EVENTS);
        }
    }

    /**
     * Очищает журнал перед новым прогоном.
     */
    public void clear() {
        synchronized (StartupLog.EVENTS) {
            StartupLog.EVENTS.clear();
        }
    }

    /**
     * Порядковый номер этапа — по нему проверяется относительный порядок.
     * @param event Событие
     * @return Порядковый номер этапа
     */
    public int indexOf(final String event) {
        return this.events().indexOf(event);
    }

    /**
     * Случился ли этап вообще.
     * @param event Событие
     * @return Случился ли этап вообще
     */
    public boolean has(final String event) {
        return this.events().contains(event);
    }
}
