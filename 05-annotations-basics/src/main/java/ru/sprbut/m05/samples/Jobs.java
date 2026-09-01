/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Schedule;
import ru.sprbut.m05.declarations.Schedules;

/**
 * Методы с нулём, одним и несколькими вхождениями повторяемой аннотации.
 *
 * <p>Три случая нужны все три: именно на границе между «одно» и «два» меняется
 * то, что реально лежит в байткоде.</p>
 *
 * @since 1.0
 */
@SuppressWarnings("unused")
public class Jobs {

    /**
     * Открытый конструктор: экземпляр создаёт контейнер.
     */
    public Jobs() {
        // нечего инициализировать
    }

    /**
     * Без расписания вовсе.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    public void notScheduled() {
        // тело намеренно пустое
    }

    /**
     * Одно вхождение — в байткоде лежит сама аннотация.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @Schedule(cron = "0 0 * * * *")
    public void hourly() {
        // тело намеренно пустое
    }

    /**
     * Два вхождения — в байткоде остаётся только контейнер.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @Schedule(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    @Schedule(cron = "0 0 15 * * *", zone = "Europe/Moscow")
    public void twiceADay() {
        // тело намеренно пустое
    }

    /**
     * Контейнер можно указать и вручную — результат тот же.
     */
    // @checkstyle NonStaticMethodCheck (3 lines)
    @Schedules({
        @Schedule(cron = "0 */5 * * * *"),
        @Schedule(cron = "0 */10 * * * *")
    })
    public void explicitContainer() {
        // тело намеренно пустое
    }
}
