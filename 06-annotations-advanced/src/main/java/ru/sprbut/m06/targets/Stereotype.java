package ru.sprbut.m06.targets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 48: {@code ANNOTATION_TYPE} — аннотация, которую вешают на другие аннотации.
 * <p>
 * Так строятся мета-аннотации: {@code @Component} в Spring помечен именно так,
 * и по этой метке контейнер узнаёт стереотипы, которых ещё не видел.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Stereotype {

    /**
     * Слой приложения, к которому относится помеченный стереотип.
     */
    String layer();
}
