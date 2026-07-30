package ru.sprbut.m06.extended;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Элементы аннотации со значениями конкретного использования — без слияния.
 * <p>
 * Основа, поверх которой накладываются переопределения композитных аннотаций.
 */
public final class RawAttributes {

    private final Annotation annotation;

    public RawAttributes(Annotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Значения всех элементов.
     */
    public Map<String, Object> map() {
        Map<String, Object> collected = new LinkedHashMap<>();
        for (Method element : this.annotation.annotationType().getDeclaredMethods()) {
            collected.put(element.getName(), value(element));
        }
        return collected;
    }

    /**
     * Значение одного элемента.
     */
    public Object value(Method element) {
        try {
            element.setAccessible(true);
            return element.invoke(this.annotation);
        } catch (ReflectiveOperationException denied) {
            throw new IllegalStateException("Не прочитать элемент " + element.getName(), denied);
        }
    }
}
