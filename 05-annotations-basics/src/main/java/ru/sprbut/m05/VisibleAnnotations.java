package ru.sprbut.m05;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * Аннотации, которые видны на элементе в runtime.
 * <p>
 * Важно именно слово «видны»: это не то, что написано в исходниках. Аннотации
 * с политикой {@code SOURCE} и {@code CLASS} сюда не попадут — не потому,
 * что их отфильтровали, а потому что в загруженном классе их нет.
 */
public final class VisibleAnnotations {

    private final AnnotatedElement element;

    public VisibleAnnotations(AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Имена видимых аннотаций в алфавитном порядке.
     */
    public List<String> names() {
        return Arrays.stream(this.element.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .sorted()
            .toList();
    }
}
