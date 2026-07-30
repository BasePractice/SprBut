package ru.sprbut.m15.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Собственная аннотация, работающая через AOP — ровно по той же схеме,
 * что {@code @Transactional} и {@code @Cacheable} (слайд 125).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retryable {

    int attempts() default 3;
}
