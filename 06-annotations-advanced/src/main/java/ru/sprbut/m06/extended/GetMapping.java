package ru.sprbut.m06.extended;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Композиция: {@code @GetMapping("/users")} должен превратиться
 * в {@code @RequestMapping(path = "/users", method = GET)}.
 * <p>
 * {@code value} объявлен алиасом для {@code path} мета-аннотации явно —
 * без {@link AliasFor} слияние по имени не сработало бы: имена разные.
 * <p>
 * {@code ANNOTATION_TYPE} в целях нужен, чтобы эту аннотацию можно было
 * навесить на другую и построить композицию второго уровня.
 */
@RequestMapping(method = HttpMethod.GET)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface GetMapping {

    /**
     * Путь маршрута; переопределяет {@code path} мета-аннотации.
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String value() default "/";

    /**
     * Одноимённый элемент переопределяет мета-аннотацию и без {@link AliasFor}.
     */
    String[] produces() default {};
}
