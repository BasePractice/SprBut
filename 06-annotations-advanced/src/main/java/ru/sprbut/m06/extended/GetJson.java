package ru.sprbut.m06.extended;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Второй уровень композиции: JSON-специализация {@link GetMapping}.
 * <p>
 * Путь от неё до {@link RequestMapping} — два шага, и наивная проверка
 * «на один уровень вглубь» его не пройдёт.
 */
@GetMapping(produces = "application/json")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetJson {

    /**
     * Путь маршрута.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String value() default "/";
}
