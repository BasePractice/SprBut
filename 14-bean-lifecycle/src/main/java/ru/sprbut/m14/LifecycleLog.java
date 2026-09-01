/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// журнал общий для всех бинов модуля: только так видно порядок этапов
// у разных объектов, поэтому он статический и синхронизированный
// @checkstyle NonStaticMethodCheck disable
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
@SuppressWarnings({"PMD.ProhibitPublicStaticMethods", "PMD.AvoidSynchronizedStatement"})
public final class LifecycleLog {

    /**
     * Записанные этапы, общие для всех бинов модуля.
     */
    private static final List<String> EVENTS = new ArrayList<>(0);

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public LifecycleLog() {
        // нечего инициализировать
    }

    /**
     * Записывает этап.
     * @param event Событие
     */
    public static void record(final String event) {
        synchronized (LifecycleLog.EVENTS) {
            LifecycleLog.EVENTS.add(event);
        }
    }

    /**
     * Все записанные этапы по порядку.
     * @return Все записанные этапы по порядку
     */
    public List<String> events() {
        synchronized (LifecycleLog.EVENTS) {
            return List.copyOf(LifecycleLog.EVENTS);
        }
    }

    /**
     * Очищает журнал перед новым прогоном.
     */
    public void clear() {
        synchronized (LifecycleLog.EVENTS) {
            LifecycleLog.EVENTS.clear();
        }
    }

    /**
     * Только события конкретного бина.
     * @param bean Объект
     * @return События конкретного бина
     */
    public List<String> of(final String bean) {
        return this.events().stream()
            .filter(event -> event.endsWith(String.format(":%s", bean)))
            .toList();
    }

    /**
     * Порядковый номер события — по нему проверяется относительный порядок шагов.
     * @param event Событие
     * @return Порядковый номер события
     */
    public int indexOf(final String event) {
        return this.events().indexOf(event);
    }
}
