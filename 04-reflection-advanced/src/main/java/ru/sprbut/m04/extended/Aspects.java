package ru.sprbut.m04.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотации-метаданные для {@link JdkAopFactory}.
 * <p>
 * Все три — с {@code RetentionPolicy.RUNTIME}, иначе прокси их не увидит.
 * Это прямые аналоги {@code @Retryable}, {@code @Timed} и {@code @Cacheable}
 * из экосистемы Spring.
 */
public final class Aspects {

    private Aspects() {
    }

    /** Повторить вызов при исключении. Аналог {@code @Retryable}. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Retry {
        int attempts() default 3;
    }

    /** Замерить время выполнения. Аналог {@code @Timed} из Micrometer. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Timed {
    }

    /** Закэшировать результат по аргументам. Аналог {@code @Cacheable}. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Cached {
    }

    /** Отдать заранее заданный результат, не вызывая цель вовсе. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Stubbed {
        String value();
    }
}
