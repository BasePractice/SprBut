package ru.sprbut.m04;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 36: {@link AnnotatedElement} — единый разбор аннотаций.
 * <p>
 * Класс, поле, метод, конструктор и параметр реализуют один интерфейс,
 * поэтому код чтения аннотаций пишется ровно один раз. Это и позволяет
 * Spring поддерживать {@code @Qualifier} одинаково во всех этих местах.
 */
public final class ElementAnnotations {

    private final AnnotatedElement element;

    public ElementAnnotations(AnnotatedElement element) {
        this.element = element;
    }

    /**
     * Имена runtime-аннотаций элемента в алфавитном порядке.
     */
    public List<String> names() {
        return Arrays.stream(this.element.getAnnotations())
            .map(annotation -> annotation.annotationType().getSimpleName())
            .sorted()
            .toList();
    }
}
