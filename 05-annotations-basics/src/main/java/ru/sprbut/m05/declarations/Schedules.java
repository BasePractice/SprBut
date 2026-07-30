package ru.sprbut.m05.declarations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Контейнер для повторяемой {@link Schedule}.
 * <p>
 * Требования языка к контейнеру жёсткие: элемент обязан называться {@code value},
 * иметь тип массива повторяемой аннотации, а retention контейнера — быть не уже,
 * чем у неё самой.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Schedules {

    Schedule[] value();
}
