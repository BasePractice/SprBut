package ru.sprbut.m06.members;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Значение элемента аннотации, пригодное для печати.
 * <p>
 * Массив без такой обработки печатается своим хэшем, а {@code Class} —
 * полным именем с пакетом. И то и другое делает отчёт нечитаемым ровно там,
 * где он нужен: при разборе чужой конфигурации.
 */
public final class Described {

    private final Object value;

    public Described(Object value) {
        this.value = value;
    }

    /**
     * Читаемое представление значения.
     */
    public Object text() {
        if (this.value == null) {
            return null;
        }
        if (this.value instanceof Class<?> type) {
            return type.getSimpleName();
        }
        if (this.value.getClass().isArray()) {
            return array();
        }
        if (this.value instanceof Annotation nested) {
            return "@" + nested.annotationType().getSimpleName();
        }
        return this.value;
    }

    private String array() {
        int length = Array.getLength(this.value);
        Object[] items = new Object[length];
        for (int index = 0; index < length; index++) {
            items[index] = new Described(Array.get(this.value, index)).text();
        }
        return Arrays.toString(items);
    }
}
