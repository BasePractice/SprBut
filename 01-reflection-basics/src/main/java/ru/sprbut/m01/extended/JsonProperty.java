package ru.sprbut.m01.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Переименовывает поле при сериализации. Retention обязательно RUNTIME —
 * иначе {@link ReflectiveJsonWriter} её просто не увидит.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonProperty {

    /** Имя ключа в JSON. Пустая строка — использовать имя поля как есть. */
    String value() default "";
}
