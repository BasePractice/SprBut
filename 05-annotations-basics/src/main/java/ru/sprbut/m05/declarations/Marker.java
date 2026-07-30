package ru.sprbut.m05.declarations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Слайд 42: «Для маркировки, без параметров (@Override)».
 * <p>
 * Маркерная аннотация не несёт данных — весь её смысл в самом факте присутствия.
 * Так устроены {@code @Override}, {@code @FunctionalInterface}, {@code @Entity}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Marker {
}
