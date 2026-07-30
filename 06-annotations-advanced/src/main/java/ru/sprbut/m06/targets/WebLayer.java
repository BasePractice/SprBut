package ru.sprbut.m06.targets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Пример {@link Stereotype} на аннотации — мета-аннотация читается с аннотации
 * ровно так же, как с обычного класса.
 */
@Stereotype(layer = "web")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebLayer {
}
