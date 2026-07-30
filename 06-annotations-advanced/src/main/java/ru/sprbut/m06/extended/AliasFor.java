package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Объявляет, что элемент композитной аннотации <b>переопределяет</b> элемент
 * мета-аннотации.
 * <p>
 * Это упрощённая копия {@code org.springframework.core.annotation.AliasFor}.
 * Именно так {@code @RestController("users")} задаёт {@code value} у вложенного
 * {@code @Controller}, а {@code @GetMapping("/x")} — {@code path} у
 * {@code @RequestMapping}.
 * <p>
 * В самом языке Java такого механизма нет: аннотации не наследуются и не
 * переопределяются. Всё делает код, который их читает — см.
 * {@link MergedAnnotationScanner}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AliasFor {

    /** Мета-аннотация, элемент которой переопределяется. */
    Class<? extends Annotation> annotation();

    /** Имя переопределяемого элемента. По умолчанию — имя текущего элемента. */
    String attribute() default "";
}
