/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m14;

import java.util.ArrayList;
import java.util.List;

/**
 * Общий журнал этапов жизненного цикла: все участники пишут сюда, и по итоговому
 * списку виден точный порядок восьми шагов со слайда 118.
 *
 * <p>Хранилище статическое, и это вынужденно: {@code BeanPostProcessor} создаётся
 * контейнером раньше обычных бинов и получить журнал через конструктор
 * не может — на момент его создания внедрять ещё нечего. Тот самый порядок,
 * который изучает модуль, и мешает сделать журнал обычной зависимостью.</p>
 *
 * @since 1.0
 */
public final class LifecycleLog {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public LifecycleLog() {
        // нечего инициализировать
    }

    /**
     * Значение {@code EVENTS}.
     */
    private static final List<String> EVENTS = new ArrayList<>();

    /**
     * Записывает этап.
     * @param event Событие
     */
    public static synchronized void record(final String event) {
        EVENTS.add(event);
    }

    /**
     * Все записанные этапы по порядку.
     * @return Все записанные этапы по порядку
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
     * Только события конкретного бина.
     * @param bean Объект
     * @return Только события конкретного бина
     */
    public synchronized List<String> of(final String bean) {
        return EVENTS.stream().filter(event -> event.endsWith(":" + bean)).toList();
    }

    /**
     * Порядковый номер события — по нему проверяется относительный порядок шагов.
     * @param event Событие
     * @return Порядковый номер события — по нему проверяется относительный порядок шагов
     */
    public synchronized int indexOf(final String event) {
        return EVENTS.indexOf(event);
    }
}
