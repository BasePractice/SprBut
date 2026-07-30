package ru.sprbut.m09;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Журнал перехваченных вызовов сгенерированного подкласса.
 * <p>
 * Список статический не по небрежности: ByteBuddy требует, чтобы методы
 * перехватчика были {@code static}, и добраться до состояния экземпляра
 * оттуда физически нечем. Это ограничение библиотеки, а не выбор дизайна.
 */
public final class Intercepted {

    private static final List<String> ENTRIES = new CopyOnWriteArrayList<>();

    /**
     * Добавляет запись о перехваченном вызове.
     */
    public static void add(String entry) {
        ENTRIES.add(entry);
    }

    /**
     * Очищает журнал.
     */
    public void clear() {
        ENTRIES.clear();
    }

    /**
     * Снимок журнала.
     */
    public List<String> entries() {
        return List.copyOf(ENTRIES);
    }
}
