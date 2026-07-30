package ru.sprbut.m06.members;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Слайд 52: «default-значения элементов».
 * <p>
 * Элементы аннотации — это методы её интерфейса, а значения по умолчанию
 * доступны отдельно от значений конкретного использования. На этом строятся
 * отчёты «эффективной конфигурации»: что задал пользователь, а что осталось
 * от умолчаний.
 */
public final class AnnotationMembers {

    private final Class<? extends Annotation> annotation;

    public AnnotationMembers(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    /**
     * Значения по умолчанию — только тех элементов, у которых они есть.
     * <p>
     * Обязательного элемента здесь нет вовсе, и это не упущение: «умолчание
     * отсутствует» и «умолчание равно null» — разные вещи, а положить {@code null}
     * в карту значило бы их смешать.
     */
    public Map<String, Object> defaults() {
        Map<String, Object> collected = new LinkedHashMap<>();
        for (Method element : this.annotation.getDeclaredMethods()) {
            if (element.getDefaultValue() != null) {
                collected.put(element.getName(), new Described(element.getDefaultValue()).text());
            }
        }
        return Map.copyOf(collected);
    }

    /**
     * Элементы без {@code default} — их обязаны задать при использовании.
     */
    public List<String> required() {
        return Arrays.stream(this.annotation.getDeclaredMethods())
            .filter(element -> element.getDefaultValue() == null)
            .map(Method::getName)
            .sorted()
            .toList();
    }
}
