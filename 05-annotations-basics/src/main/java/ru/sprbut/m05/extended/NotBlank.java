package ru.sprbut.m05.extended;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркерная аннотация: важен сам факт присутствия, параметров нет.
 * <p>
 * Слайд 41 называет такие «для маркировки, без параметров» — как {@code @Override}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotBlank {
}
