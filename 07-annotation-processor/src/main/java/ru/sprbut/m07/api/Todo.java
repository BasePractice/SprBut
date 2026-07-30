package ru.sprbut.m07.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 60: «Анализ исходного кода».
 * <p>
 * Процессор может вообще ничего не генерировать — только проверять код
 * и писать диагностику через {@code Messager}. Так работают Error Prone,
 * NullAway и проверки {@code @Nullable}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Todo {

    String value();

    /** {@code true} — сборка падает с ошибкой, {@code false} — только предупреждение. */
    boolean blocking() default false;
}
