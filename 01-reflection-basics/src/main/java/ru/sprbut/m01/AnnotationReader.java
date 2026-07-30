package ru.sprbut.m01;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 9: «Получать аннотации».
 * <p>
 * {@link AnnotatedElement} — общий интерфейс для {@code Class}, {@code Field},
 * {@code Method}, {@code Parameter}. Поэтому один и тот же код читает аннотации
 * с любого элемента. Важно: видны только аннотации с {@code RetentionPolicy.RUNTIME}.
 */
public final class AnnotationReader {

    private AnnotationReader() {
    }

    public static boolean isPresent(AnnotatedElement element, Class<? extends Annotation> annotation) {
        return element.isAnnotationPresent(annotation);
    }

    public static <A extends Annotation> Optional<A> find(AnnotatedElement element, Class<A> annotation) {
        return Optional.ofNullable(element.getAnnotation(annotation));
    }

    /**
     * Все runtime-аннотации элемента. Аннотации с retention SOURCE/CLASS
     * сюда не попадут — их в байткоде либо нет вовсе, либо они не читаются JVM.
     */
    public static List<String> names(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotations())
                .map(a -> a.annotationType().getSimpleName())
                .sorted()
                .toList();
    }
}
