package ru.sprbut.m07.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 58: «Генерация исходного кода».
 * <p>
 * Помеченный класс получит рядом сгенерированный {@code XxxBuilder}.
 * <p>
 * Retention намеренно {@code SOURCE}: аннотация нужна только компилятору
 * и процессору. В байткод она не попадёт вовсе — в runtime про неё никто
 * не узнает, и это правильно. Так же устроены аннотации Lombok.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateBuilder {

    /** Суффикс имени генерируемого класса. */
    String suffix() default "Builder";
}
