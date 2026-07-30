package ru.sprbut.m05.samples;

import ru.sprbut.m05.declarations.Schedule;
import ru.sprbut.m05.declarations.Schedules;

/**
 * Методы с нулём, одним и несколькими вхождениями повторяемой аннотации.
 * <p>
 * Три случая нужны все три: именно на границе между «одно» и «два» меняется
 * то, что реально лежит в байткоде.
 */
@SuppressWarnings("unused")
public class Jobs {

    /**
     * Без расписания вовсе.
     */
    public void notScheduled() {
    }

    /**
     * Одно вхождение — в байткоде лежит сама аннотация.
     */
    @Schedule(cron = "0 0 * * * *")
    public void hourly() {
    }

    /**
     * Два вхождения — в байткоде остаётся только контейнер.
     */
    @Schedule(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    @Schedule(cron = "0 0 15 * * *", zone = "Europe/Moscow")
    public void twiceADay() {
    }

    /**
     * Контейнер можно указать и вручную — результат тот же.
     */
    @Schedules({
        @Schedule(cron = "0 */5 * * * *"),
        @Schedule(cron = "0 */10 * * * *")
    })
    public void explicitContainer() {
    }
}
