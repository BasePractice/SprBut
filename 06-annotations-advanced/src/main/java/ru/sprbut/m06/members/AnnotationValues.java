package ru.sprbut.m06.members;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Фактические значения элементов конкретного использования аннотации.
 * <p>
 * Незаданные элементы возвращают свои умолчания — отличить одно от другого
 * можно только сравнив с {@link AnnotationMembers#defaults()}. Ровно так
 * устроен отчёт {@code /actuator/configprops}.
 */
public final class AnnotationValues {

    private final Annotation annotation;

    public AnnotationValues(Annotation annotation) {
        this.annotation = annotation;
    }

    /**
     * Значения всех элементов.
     */
    public Map<String, Object> values() {
        Map<String, Object> collected = new LinkedHashMap<>();
        for (Method element : this.annotation.annotationType().getDeclaredMethods()) {
            collected.put(element.getName(), new Described(read(element)).text());
        }
        return Map.copyOf(collected);
    }

    private Object read(Method element) {
        try {
            element.setAccessible(true);
            return element.invoke(this.annotation);
        } catch (ReflectiveOperationException denied) {
            throw new IllegalStateException("Не прочитать элемент " + element.getName(), denied);
        }
    }
}
