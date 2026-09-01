/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ru.sprbut.m05.declarations.Schedule;
import ru.sprbut.m05.declarations.Schedules;

/**
 * Слайд 42: «Repeatable — одна и та же аннотация много раз».
 *
 * <p>Повторяемость — синтаксический сахар. Компилятор берёт несколько экземпляров
 * и заворачивает их в аннотацию-контейнер. Отсюда неочевидное поведение:
 * <ul>
 * <li>одно вхождение — в байткоде лежит сама аннотация;</li>
 * <li>два и более — в байткоде лежит <b>только контейнер</b>, и
 * {@code getAnnotation(Schedule.class)} вернёт {@code null}.</li>
 * </ul>
 * Правильный способ читать всегда один: {@code getAnnotationsByType}, который
 * разворачивает контейнер прозрачно.</p>
 *
 * @since 1.0
 */
public final class Repeated {

    /**
     * Метод.
     */
    private final Method method;

    /**
     * Основной конструктор.
     * @param method Метод
     */
    public Repeated(final Method method) {
        this.method = method;
    }

    /**
     * Все вхождения — надёжный способ, работает и для одного, и для нескольких.
     * @return Все вхождения — надёжный способ, работает и для одного, и для нескольких
     */
    public List<Schedule> all() {
        return Arrays.asList(this.method.getAnnotationsByType(Schedule.class));
    }

    /**
     * Наивный способ: ломается ровно тогда, когда аннотаций становится больше одной.
     * @return Наивный способ: ломается ровно тогда, когда аннотаций становится больше одной
     */
    public Optional<Schedule> single() {
        return Optional.ofNullable(this.method.getAnnotation(Schedule.class));
    }

    /**
     * Контейнер, если компилятор его создал.
     * @return Контейнер, если компилятор его создал
     */
    public Optional<Schedules> container() {
        return Optional.ofNullable(this.method.getAnnotation(Schedules.class));
    }

    /**
     * Расписания в виде cron-выражений.
     * @return Расписания в виде cron-выражений
     */
    public List<String> crons() {
        return this.all().stream().map(Schedule::cron).toList();
    }
}
