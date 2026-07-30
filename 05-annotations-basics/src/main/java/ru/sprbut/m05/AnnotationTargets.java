package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 39: {@code @Target{TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE}}.
 * <p>
 * {@code @Target} — <b>ограничение компилятора</b>, а не рантайма. Он не даст
 * поставить аннотацию туда, где её быть не должно; в runtime проверять уже нечего,
 * потому что неправильный код просто не собрался бы.
 * <p>
 * Отдельная тонкость: <b>отсутствие</b> {@code @Target} означает «можно почти
 * везде», а не «нигде».
 */
public final class AnnotationTargets {

    private final Class<? extends Annotation> annotation;

    public AnnotationTargets(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Куда разрешено ставить аннотацию — читается из её собственной мета-аннотации.
     */
    public List<ElementType> allowed() {
        Target target = this.annotation.getAnnotation(Target.class);
        if (target == null) {
            return List.of();
        }
        return Arrays.asList(target.value());
    }

    /**
     * Можно ли поставить аннотацию на поле.
     */
    public boolean fields() {
        return allowed().contains(ElementType.FIELD);
    }

    /**
     * Можно ли поставить аннотацию на класс.
     */
    public boolean types() {
        return allowed().contains(ElementType.TYPE);
    }
}
