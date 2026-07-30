package ru.sprbut.m18;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал этапов запуска: все слушатели и хуки пишут сюда, и по итоговому списку
 * восстанавливается вся последовательность со слайда 172 (СХЕМА 11).
 * <p>
 * Хранилище статическое по той же причине, что и в модуле 14, только острее:
 * {@code ApplicationContextInitializer} и ранние слушатели вызываются до того,
 * как контекст вообще существует. Внедрить им зависимость физически некуда.
 */
public final class StartupLog {

    private static final List<String> EVENTS = new ArrayList<>();

    /**
     * Записывает этап запуска.
     */
    public static synchronized void record(String event) {
        EVENTS.add(event);
    }

    /**
     * Все записанные этапы по порядку.
     */
    public synchronized List<String> events() {
        return List.copyOf(EVENTS);
    }

    /**
     * Очищает журнал перед новым прогоном.
     */
    public synchronized void clear() {
        EVENTS.clear();
    }

    /**
     * Порядковый номер этапа — по нему проверяется относительный порядок.
     */
    public synchronized int indexOf(String event) {
        return EVENTS.indexOf(event);
    }

    /**
     * Случился ли этап вообще.
     */
    public synchronized boolean has(String event) {
        return EVENTS.contains(event);
    }
}
