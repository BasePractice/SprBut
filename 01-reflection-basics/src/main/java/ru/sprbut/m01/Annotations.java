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
 * {@code Method} и {@code Parameter}, поэтому один и тот же код читает аннотации
 * с любого элемента.
 * <p>
 * Видны только аннотации с {@code RetentionPolicy.RUNTIME}. Остальных здесь нет
 * не потому, что их не нашли, а потому, что в загруженном классе их физически
 * не существует.
 */
public final class Annotations {

    private final AnnotatedElement element;

    public Annotations(AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Присутствует ли аннотация на элементе.
     */
    public boolean has(Class<? extends Annotation> type) {
        return this.element.isAnnotationPresent(type);
    }

    /**
     * Аннотация, если она есть.
     */
    public <A extends Annotation> Optional<A> find(Class<A> type) {
        return Optional.ofNullable(this.element.getAnnotation(type));
    }

    /**
     * Имена всех runtime-аннотаций элемента в алфавитном порядке.
     */
    public List<String> names() {
        return Arrays.stream(this.element.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .sorted()
            .toList();
    }
}
