package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Правило для повторяемого {@link Matches}.
 * <p>
 * Читает через {@code getAnnotationsByType} — единственный способ, работающий
 * и для одного вхождения, и для нескольких. С {@code getAnnotation} второй
 * шаблон потерялся бы молча.
 */
public final class MatchesRule implements Rule {

    @Override
    public List<Violation> check(Field field, Object value) {
        Matches[] patterns = field.getAnnotationsByType(Matches.class);
        if (patterns.length == 0 || value == null) {
            return List.of();
        }
        List<Violation> found = new ArrayList<>();
        String text = String.valueOf(value);
        for (Matches each : patterns) {
            try {
                if (!Pattern.matches(each.regex(), text)) {
                    found.add(new Violation(
                        field.getName(), each.message() + " '" + each.regex() + "'", value
                    ));
                }
            } catch (PatternSyntaxException malformed) {
                found.add(new Violation(
                    field.getName(), "некорректный шаблон '" + each.regex() + "'", value
                ));
            }
        }
        return List.copyOf(found);
    }
}
