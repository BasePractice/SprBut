package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Расширенный пример модуля 05.</b>
 * <p>
 * Объект, проверенный по собственным аннотациям, — работающая мини-версия
 * Bean Validation. Показывает все виды аннотаций из презентации в деле:
 * <ul>
 *   <li>маркерная {@link NotBlank} — важен сам факт присутствия;</li>
 *   <li>single-value {@link MaxLength} — {@code @MaxLength(10)} без имени элемента;</li>
 *   <li>многоэлементная {@link Range} со значениями по умолчанию;</li>
 *   <li>повторяемая {@link Matches} — только через {@code getAnnotationsByType};</li>
 *   <li>{@link InvisibleNotNull} с retention {@code CLASS} — движок её не видит,
 *       и это ровно та ошибка, которую совершают, забыв {@code @Retention(RUNTIME)}.</li>
 * </ul>
 * Главный вывод здесь тот же, что во всём курсе: аннотация ничего не проверяет.
 * Проверяет тот, кто её прочитал.
 */
public final class Validated {

    private final Object target;

    private final List<Rule> rules;

    public Validated(Object target) {
        this(target, List.of(
            new NotBlankRule(), new MaxLengthRule(), new RangeRule(), new MatchesRule()
        ));
    }

    public Validated(Object target, List<Rule> rules) {
        this.target = target;
        this.rules = List.copyOf(rules);
    }

    /**
     * Итог проверки по всем ограничениям полей объекта и его суперклассов.
     */
    public Verdict verdict() {
        List<Violation> found = new ArrayList<>();
        for (Field field : new ConstrainedFields(this.target.getClass()).list()) {
            field.setAccessible(true);
            Object value = read(field);
            for (Rule rule : this.rules) {
                found.addAll(rule.check(field, value));
            }
        }
        return new Verdict(found);
    }

    /**
     * Проверка в режиме «падать сразу»: удобна там, где продолжать
     * с некорректным объектом бессмысленно.
     */
    public void check() {
        Verdict verdict = verdict();
        if (!verdict.valid()) {
            throw new ConstraintsViolated(verdict);
        }
    }

    private Object read(Field field) {
        try {
            return field.get(this.target);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Поле " + field.getName() + " недоступно", denied);
        }
    }
}
