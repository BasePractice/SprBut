package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Правило для маркерной аннотации {@link NotBlank}: важен сам факт присутствия.
 */
public final class NotBlankRule implements Rule {

    @Override
    public List<Violation> check(Field field, Object value) {
        if (!field.isAnnotationPresent(NotBlank.class)) {
            return List.of();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return List.of(new Violation(field.getName(), "значение обязательно", value));
        }
        return List.of();
    }
}
