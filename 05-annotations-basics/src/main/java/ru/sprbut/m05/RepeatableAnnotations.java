package ru.sprbut.m05;

import ru.sprbut.m05.declarations.Schedule;
import ru.sprbut.m05.declarations.Schedules;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 44: {@code @Repeatable}.
 * <p>
 * Повторяемость — синтаксический сахар. Компилятор берёт несколько экземпляров
 * аннотации и заворачивает их в аннотацию-контейнер. Отсюда неочевидное поведение:
 * <ul>
 *   <li>одно вхождение — в байткоде лежит сама аннотация;</li>
 *   <li>два и более — в байткоде лежит <b>только контейнер</b>, а
 *       {@code getAnnotation(Schedule.class)} вернёт {@code null}.</li>
 * </ul>
 * Правильный способ читать всегда один: {@code getAnnotationsByType}, который
 * прозрачно разворачивает контейнер.
 */
public final class RepeatableAnnotations {

    private RepeatableAnnotations() {
    }

    /** Надёжный способ: работает и для одного вхождения, и для нескольких. */
    public static List<Schedule> allSchedules(Method method) {
        return Arrays.asList(method.getAnnotationsByType(Schedule.class));
    }

    /** Наивный способ: ломается ровно тогда, когда аннотаций становится больше одной. */
    public static Optional<Schedule> naiveSingleSchedule(Method method) {
        return Optional.ofNullable(method.getAnnotation(Schedule.class));
    }

    /** Что на самом деле лежит в байткоде. */
    public static List<String> rawAnnotationNames(Method method) {
        return Arrays.stream(method.getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .sorted()
                .toList();
    }

    public static Optional<Schedules> container(Method method) {
        return Optional.ofNullable(method.getAnnotation(Schedules.class));
    }

    public static List<String> cronExpressions(Method method) {
        return allSchedules(method).stream().map(Schedule::cron).toList();
    }

    /** Подопытные методы: ноль, одно и несколько вхождений. */
    @SuppressWarnings("unused")
    public static class Jobs {

        public void notScheduled() {
        }

        @Schedule(cron = "0 0 * * * *")
        public void hourly() {
        }

        @Schedule(cron = "0 0 3 * * *", zone = "Europe/Moscow")
        @Schedule(cron = "0 0 15 * * *", zone = "Europe/Moscow")
        public void twiceADay() {
        }

        /** Контейнер можно указать и вручную — результат тот же. */
        @Schedules({
                @Schedule(cron = "0 */5 * * * *"),
                @Schedule(cron = "0 */10 * * * *")
        })
        public void explicitContainer() {
        }
    }
}
