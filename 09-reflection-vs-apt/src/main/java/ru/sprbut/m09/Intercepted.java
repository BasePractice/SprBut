/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m09;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Журнал перехваченных вызовов сгенерированного подкласса.
 *
 * <p>Список статический не по небрежности: ByteBuddy требует, чтобы методы
 * перехватчика были {@code static}, и добраться до состояния экземпляра
 * оттуда физически нечем. Это ограничение библиотеки, а не выбор дизайна.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class Intercepted {

    /**
     * Значение {@code ENTRIES}.
     */
    private static final List<String> ENTRIES = new CopyOnWriteArrayList<>();

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Intercepted() {
        // нечего инициализировать
    }

    /**
     * Добавляет запись о перехваченном вызове.
     * @param entry Запись о вызове
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public static void add(final String entry) {
        ENTRIES.add(entry);
    }

    /**
     * Очищает журнал.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public void clear() {
        ENTRIES.clear();
    }

    /**
     * Снимок журнала.
     * @return Снимок журнала
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public List<String> entries() {
        return List.copyOf(ENTRIES);
    }
}
