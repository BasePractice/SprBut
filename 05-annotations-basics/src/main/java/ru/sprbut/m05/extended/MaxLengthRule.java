package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Правило для {@link MaxLength}: значение элемента аннотации становится
 * параметром проверки.
 */
public final class MaxLengthRule implements Rule {

    @Override
    public List<Violation> check(Field field, Object value) {
        MaxLength limit = field.getAnnotation(MaxLength.class);
        if (limit == null || value == null) {
            return List.of();
        }
        int length = String.valueOf(value).length();
        if (length > limit.value()) {
            return List.of(new Violation(
                field.getName(),
                "длина " + length + " превышает максимум " + limit.value(),
                value
            ));
        }
        return List.of();
    }
}
