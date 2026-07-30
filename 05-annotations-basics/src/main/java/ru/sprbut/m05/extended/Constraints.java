package ru.sprbut.m05.extended;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Набор ограничений для {@link ValidationEngine} — мини-версия Bean Validation
 * (`jakarta.validation`), собранная на средствах слайдов 38–45.
 * <p>
 * Здесь встречаются все виды аннотаций, перечисленные в презентации:
 * маркерная, с единственным элементом {@code value}, с несколькими элементами
 * и значениями по умолчанию, повторяемая.
 */
public final class Constraints {

    private Constraints() {
    }

    /** Маркерная: смысл в самом факте присутствия (слайд 42). */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotBlank {
    }

    /** С единственным элементом {@code value} — имя при использовании опускается (слайд 43). */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MaxLength {
        int value();
    }

    /** Несколько элементов со значениями по умолчанию. */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Range {
        long min() default Long.MIN_VALUE;

        long max() default Long.MAX_VALUE;

        String message() default "значение вне допустимого диапазона";
    }

    /** Повторяемая: на одно поле можно навесить несколько шаблонов (слайд 44). */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Repeatable(Matches.All.class)
    public @interface Matches {

        String regex();

        String message() default "значение не соответствует шаблону";

        @Documented
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        @interface All {
            Matches[] value();
        }
    }

    /**
     * Ограничение с retention CLASS — чтобы показать, что движок его <b>не увидит</b>.
     * Это ровно та ошибка, которую делают, забыв {@code @Retention(RUNTIME)}.
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.FIELD)
    public @interface InvisibleNotNull {
    }
}
