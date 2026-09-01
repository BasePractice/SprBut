/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Расширенный пример модуля 05.</b>
 *
 * <p>Объект, проверенный по собственным аннотациям, — работающая мини-версия
 * Bean Validation. Показывает все виды аннотаций из презентации в деле:
 * <ul>
 * <li>маркерная {@link NotBlank} — важен сам факт присутствия;</li>
 * <li>single-value {@link MaxLength} — {@code @MaxLength(10)} без имени элемента;</li>
 * <li>многоэлементная {@link Range} со значениями по умолчанию;</li>
 * <li>повторяемая {@link Matches} — только через {@code getAnnotationsByType};</li>
 * <li>{@link InvisibleNotNull} с retention {@code CLASS} — движок её не видит,
 * и это ровно та ошибка, которую совершают, забыв {@code @Retention(RUNTIME)}.</li>
 * </ul>
 * Главный вывод здесь тот же, что во всём курсе: аннотация ничего не проверяет.
 * Проверяет тот, кто её прочитал.</p>
 *
 * @since 1.0
 */
public final class Validated {

    /**
     * Целевой объект.
     */
    private final Object target;

    /**
     * Правила.
     */
    private final List<Rule> rules;

    /**
     * Основной конструктор.
     * @param target Целевой объект
     */
    public Validated(final Object target) {
        this(target, List.of(
            new NotBlankRule(), new MaxLengthRule(), new RangeRule(), new MatchesRule()
        ));
    }

    /**
     * Основной конструктор.
     * @param target Целевой объект
     * @param rules Правила
     */
    public Validated(final Object target, final List<Rule> rules) {
        this.target = target;
        this.rules = List.copyOf(rules);
    }

    /**
     * Проверка в режиме «падать сразу»: удобна там, где продолжать
     * с некорректным объектом бессмысленно.
     */
    public void check() {
        final Verdict verdict = this.verdict();
        if (!verdict.valid()) {
            throw new ConstraintsViolated(verdict);
        }
    }

    /**
     * Итог проверки по всем ограничениям полей объекта и его суперклассов.
     * @return Итог проверки по всем ограничениям полей объекта и его суперклассов
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Verdict verdict() {
        final List<Violation> found = new ArrayList<>();
        for (final Field field : new ConstrainedFields(this.target.getClass()).list()) {
            field.setAccessible(true);
            final Object value = this.read(field);
            for (final Rule rule : this.rules) {
                found.addAll(rule.check(field, value));
            }
        }
        return new Verdict(found);
    }

    private Object read(final Field field) {
        try {
            return field.get(this.target);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException("Поле " + field.getName() + " недоступно", denied);
        }
    }
}
