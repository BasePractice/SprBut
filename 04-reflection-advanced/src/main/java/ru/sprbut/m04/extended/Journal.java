package ru.sprbut.m04.extended;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал того, что делали аспекты.
 * <p>
 * Существует ради проверяемости: без него единственным свидетельством работы
 * аспекта было бы время выполнения, а на нём тесты писать нельзя.
 * Синхронизация не украшение — один прокси может обслуживать несколько потоков.
 */
public final class Journal {

    private final List<String> entries;

    public Journal() {
        this(new ArrayList<>());
    }

    public Journal(List<String> entries) {
        this.entries = entries;
    }

    /**
     * Добавляет запись.
     */
    public synchronized void record(String entry) {
        this.entries.add(entry);
    }

    /**
     * Снимок журнала.
     */
    public synchronized List<String> entries() {
        return List.copyOf(this.entries);
    }

    /**
     * Сколько записей начинается с указанного префикса.
     */
    public synchronized long count(String prefix) {
        return this.entries.stream().filter(entry -> entry.startsWith(prefix)).count();
    }
}
