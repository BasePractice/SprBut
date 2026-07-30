package ru.sprbut.m14;

import java.util.ArrayList;
import java.util.List;

/**
 * Общий журнал этапов жизненного цикла. Все участники пишут сюда,
 * и по итоговому списку видно точный порядок восьми шагов со слайда 118.
 */
public final class LifecycleLog {

    private static final List<String> EVENTS = new ArrayList<>();

    private LifecycleLog() {
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

    /** Только события конкретного бина. */
    public static synchronized List<String> eventsOf(String beanName) {
        return EVENTS.stream().filter(e -> e.endsWith(":" + beanName)).toList();
    }

    /** Порядковый номер события — по нему проверяется относительный порядок шагов. */
    public static synchronized int indexOf(String event) {
        return EVENTS.indexOf(event);
    }
}
