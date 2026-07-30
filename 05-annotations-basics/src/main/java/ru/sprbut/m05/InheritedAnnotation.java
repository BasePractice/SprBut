package ru.sprbut.m05;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Слайд 41: {@code @Inherited} и три границы, за которые оно не работает.
 * <p>
 * Наследуется только аннотация <b>класса</b>, только от суперкласса и только
 * если у неё самой есть {@code @Inherited}. Метод не наследует аннотацию
 * никогда, интерфейс — тоже никогда.
 * <p>
 * Именно из-за этих ограничений Spring не полагается на {@code @Inherited},
 * а обходит иерархию сам ({@code AnnotatedElementUtils}). Как выглядит такой
 * обход — в {@link HierarchySearch} и в модуле 06.
 */
public final class InheritedAnnotation<A extends Annotation> {

    private final Class<?> type;

    private final Class<A> annotation;

    public InheritedAnnotation(Class<?> type, Class<A> annotation) {
        this.type = type;
        this.annotation = annotation;
    }

    /**
     * Аннотация с учётом {@code @Inherited}.
     */
    public Optional<A> found() {
        return Optional.ofNullable(this.type.getAnnotation(this.annotation));
    }

    /**
     * Аннотация, объявленная на самом классе: наследование игнорируется.
     */
    public Optional<A> declared() {
        return Optional.ofNullable(this.type.getDeclaredAnnotation(this.annotation));
    }
}
