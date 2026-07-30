package ru.sprbut.m06.members;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Вложенная аннотация — тоже допустимый тип элемента.
 * <p>
 * {@code @Target({})} означает «нигде нельзя ставить напрямую»: эта аннотация
 * существует только как значение элемента другой.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Retry {

    /**
     * Число попыток.
     */
    int attempts() default 1;

    /**
     * Пауза между попытками.
     */
    long backoffMillis() default 0L;
}
