package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Правило для {@link Range}.
 * <p>
 * Аннотация на поле неподходящего типа — тоже нарушение, и молчать о нём нельзя:
 * компилятор такую ошибку не поймает, {@code @Target(FIELD)} разрешает любое поле.
 */
public final class RangeRule implements Rule {

    @Override
    public List<Violation> check(Field field, Object value) {
        Range range = field.getAnnotation(Range.class);
        if (range == null || value == null) {
            return List.of();
        }
        if (!(value instanceof Number number)) {
            return List.of(new Violation(
                field.getName(),
                "@Range применим только к числам, а поле имеет тип "
                    + field.getType().getSimpleName(),
                value
            ));
        }
        if (number.longValue() < range.min() || number.longValue() > range.max()) {
            return List.of(new Violation(
                field.getName(),
                range.message() + " [" + range.min() + ", " + range.max() + "]",
                value
            ));
        }
        return List.of();
    }
}
