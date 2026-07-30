package ru.sprbut.m18;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал этапов запуска. Все слушатели и хуки пишут сюда, и по итоговому
 * списку восстанавливается вся последовательность со слайда 172 (СХЕМА 11).
 */
public final class StartupLog {

    private static final List<String> EVENTS = new ArrayList<>();

    private StartupLog() {
    }

    public static synchronized void record(String event) {
        EVENTS.add(event);
    }

    public static synchronized List<String> events() {
        return List.copyOf(EVENTS);
    }

    public static synchronized void clear() {
        EVENTS.clear();
    }

    public static synchronized int indexOf(String event) {
        return EVENTS.indexOf(event);
    }

    public static synchronized boolean contains(String event) {
        return EVENTS.contains(event);
    }
}
