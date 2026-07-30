package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <b>Расширенный пример модуля 05.</b>
 * <p>
 * Движок валидации на собственных аннотациях — работающая мини-версия
 * Bean Validation. Показывает все виды аннотаций из презентации в деле:
 * <ul>
 *   <li>маркерная {@link Constraints.NotBlank} — важен сам факт присутствия;</li>
 *   <li>single-value {@link Constraints.MaxLength} — {@code @MaxLength(10)};</li>
 *   <li>многоэлементная {@link Constraints.Range} со значениями по умолчанию;</li>
 *   <li>повторяемая {@link Constraints.Matches} — читается через
 *       {@code getAnnotationsByType}, иначе второе вхождение потеряется;</li>
 *   <li>{@link Constraints.InvisibleNotNull} с retention {@code CLASS} —
 *       движок её не видит, и это ровно та ошибка, которую совершают,
 *       забыв {@code @Retention(RUNTIME)}.</li>
 * </ul>
 * Валидация наследуется по иерархии классов: движок обходит суперклассы сам,
 * потому что {@code @Inherited} на поля не распространяется в принципе.
 */
public final class ValidationEngine {

    private ValidationEngine() {
    }

    /** Одно нарушение: какое поле, что не так, какое было значение. */
    public record Violation(String field, String message, Object rejectedValue) {

        @Override
        public String toString() {
            return field + ": " + message + " (было: " + rejectedValue + ")";
        }
    }

    /** Итог проверки. */
    public record Result(List<Violation> violations) {

        public Result {
            violations = List.copyOf(violations);
        }

        public boolean valid() {
            return violations.isEmpty();
        }

        public List<String> messages() {
            return violations.stream().map(Violation::toString).toList();
        }

        public List<String> invalidFields() {
            return violations.stream().map(Violation::field).distinct().sorted().toList();
        }
    }

    /**
     * Проверяет объект по всем ограничениям, объявленным на его полях
     * и полях его суперклассов.
     */
    public static Result validate(Object target) {
        List<Violation> violations = new ArrayList<>();
        for (Field field : constrainedFields(target.getClass())) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(target);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Поле " + field.getName() + " недоступно", e);
            }
            checkNotBlank(field, value, violations);
            checkMaxLength(field, value, violations);
            checkRange(field, value, violations);
            checkMatches(field, value, violations);
        }
        return new Result(violations);
    }

    /** Бросает исключение вместо возврата результата — вариант «fail fast». */
    public static void validateOrThrow(Object target) {
        Result result = validate(target);
        if (!result.valid()) {
            throw new ConstraintViolationException(result);
        }
    }

    /**
     * Все поля по цепочке наследования. {@code @Inherited} тут не помог бы:
     * оно действует только на аннотации <i>классов</i>, но не полей.
     */
    static List<Field> constrainedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    private static void checkNotBlank(Field field, Object value, List<Violation> sink) {
        if (!field.isAnnotationPresent(Constraints.NotBlank.class)) {
            return;
        }
        if (value == null || String.valueOf(value).isBlank()) {
            sink.add(new Violation(field.getName(), "значение обязательно", value));
        }
    }

    private static void checkMaxLength(Field field, Object value, List<Violation> sink) {
        Constraints.MaxLength constraint = field.getAnnotation(Constraints.MaxLength.class);
        if (constraint == null || value == null) {
            return;
        }
        int length = String.valueOf(value).length();
        if (length > constraint.value()) {
            sink.add(new Violation(field.getName(),
                    "длина " + length + " превышает максимум " + constraint.value(), value));
        }
    }

    private static void checkRange(Field field, Object value, List<Violation> sink) {
        Constraints.Range constraint = field.getAnnotation(Constraints.Range.class);
        if (constraint == null || value == null) {
            return;
        }
        if (!(value instanceof Number number)) {
            sink.add(new Violation(field.getName(),
                    "@Range применим только к числам, а поле имеет тип "
                            + field.getType().getSimpleName(), value));
            return;
        }
        long asLong = number.longValue();
        if (asLong < constraint.min() || asLong > constraint.max()) {
            sink.add(new Violation(field.getName(),
                    constraint.message() + " [" + constraint.min() + ", " + constraint.max() + "]", value));
        }
    }

    /**
     * Повторяемое ограничение. Читать <b>обязательно</b> через
     * {@code getAnnotationsByType}: при двух и более вхождениях
     * {@code getAnnotation(Matches.class)} вернёт null, потому что в байткоде
     * лежит контейнер {@code Matches.All}.
     */
    private static void checkMatches(Field field, Object value, List<Violation> sink) {
        Constraints.Matches[] constraints = field.getAnnotationsByType(Constraints.Matches.class);
        if (constraints.length == 0 || value == null) {
            return;
        }
        String text = String.valueOf(value);
        for (Constraints.Matches constraint : constraints) {
            try {
                if (!Pattern.matches(constraint.regex(), text)) {
                    sink.add(new Violation(field.getName(),
                            constraint.message() + " '" + constraint.regex() + "'", value));
                }
            } catch (PatternSyntaxException e) {
                sink.add(new Violation(field.getName(),
                        "некорректный шаблон '" + constraint.regex() + "'", value));
            }
        }
    }

    /** Исключение режима «fail fast». */
    public static class ConstraintViolationException extends RuntimeException {

        private final transient Result result;

        public ConstraintViolationException(Result result) {
            super("Нарушений: " + result.violations().size() + " — "
                    + String.join("; ", result.messages()));
            this.result = result;
        }

        public Result result() {
            return result;
        }
    }
}
