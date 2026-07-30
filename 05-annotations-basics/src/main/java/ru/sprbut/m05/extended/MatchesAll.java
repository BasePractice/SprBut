package ru.sprbut.m05.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Контейнер для повторяемого {@link Matches}.
 * <p>
 * Его пишет компилятор, но объявить контейнер обязан программист — иначе
 * {@code @Repeatable} не скомпилируется. Указать его вручную тоже можно,
 * результат будет тот же.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MatchesAll {

    /**
     * Все вхождения.
     */
    Matches[] value();
}
