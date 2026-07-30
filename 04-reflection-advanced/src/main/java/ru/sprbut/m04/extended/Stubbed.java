package ru.sprbut.m04.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Отдать заранее заданный результат, не вызывая цель вовсе.
 * <p>
 * Крайний случай аспекта: поведение метода полностью заменяется метаданными.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Stubbed {

    /**
     * Значение, которое вернёт метод.
     */
    String value();
}
