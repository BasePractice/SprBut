package ru.sprbut.m04.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Повторить вызов при исключении. Учебный аналог {@code @Retryable}.
 * <p>
 * {@code RUNTIME} обязателен: прокси читает аннотацию во время работы,
 * и с любым другим retention её бы просто не существовало.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retry {

    /**
     * Сколько всего попыток сделать, включая первую.
     */
    int attempts() default 3;
}
