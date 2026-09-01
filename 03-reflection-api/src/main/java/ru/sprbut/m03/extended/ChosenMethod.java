/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Метод, выбранный по имени и числу аргументов, вместе с их привязкой.
 *
 * <p>Поиск идёт вверх по иерархии, bridge- и синтетические методы отсеиваются —
 * иначе один и тот же метод нашёлся бы дважды. При равных кандидатах точное
 * совпадение арности предпочитается varargs-раскрытию: {@code sum(1, 2)}
 * должен попасть в {@code sum(int, int)}, а не в {@code sum(int...)}.</p>
 *
 * @since 1.0
 */
public final class ChosenMethod {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Имя.
     */
    private final String name;

    /**
     * Аргументы.
     */
    private final List<String> args;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param name Имя
     * @param args Аргументы
     */
    public ChosenMethod(final Class<?> type, final String name, final List<String> args) {
        this.type = type;
        this.name = name;
        this.args = List.copyOf(args);
    }

    /**
     * Подходящий метод.
     * @return Подходящий метод
     */
    public Method method() {
        final List<Method> candidates = new ArrayList<>();
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            for (final Method candidate : current.getDeclaredMethods()) {
                if (candidate.getName().equals(this.name)
                    && !candidate.isBridge()
                    && !candidate.isSynthetic()
                    && this.fits(candidate)) {
                    candidates.add(candidate);
                }
            }
        }
        return candidates.stream()
            .min(
                Comparator.comparingInt((Method each) -> each.isVarArgs() ? 1 : 0)
                    .thenComparingInt(
                        each -> new AccessRank(each).value()
                    )
            )
            .orElseThrow(() -> new IllegalArgumentException(
                "У " + this.type.getSimpleName() + " нет метода '" + this.name
                    + "' с " + this.args.size() + " аргументами"
            ));
    }

    /**
     * Аргументы, приведённые к типам параметров.
     *
     * <p>Хвост varargs упаковывается через {@link Array#newInstance}: обычным
     * {@code new} это сделать нельзя, тип элемента известен только в runtime.</p>
     * @return Аргументы, приведённые к типам параметров
     */
    public Object[] arguments() {
        final Method chosen = this.method();
        final Class<?>[] parameters = chosen.getParameterTypes();
        final Object[] values = new Object[parameters.length];
        if (!chosen.isVarArgs()) {
            for (int index = 0; index < parameters.length; index++) {
                values[index] = new Argument(this.args.get(index), parameters[index]).value();
            }
            return values;
        }
        final int fixed = parameters.length - 1;
        for (int index = 0; index < fixed; index++) {
            values[index] = new Argument(this.args.get(index), parameters[index]).value();
        }
        final Class<?> component = parameters[fixed].getComponentType();
        final Object tail = Array.newInstance(component, this.args.size() - fixed);
        for (int index = 0; index < this.args.size() - fixed; index++) {
            Array.set(tail, index, new Argument(this.args.get(fixed + index), component).value());
        }
        values[fixed] = tail;
        return values;
    }

    /**
     * Результат вызова метода на этом объекте.
     * @param instance Экземпляр
     * @return Результат вызова метода на этом объекте
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object result(final Object instance) {
        final Method chosen = this.method();
        chosen.setAccessible(true);
        try {
            return chosen.invoke(
                Modifier.isStatic(chosen.getModifiers()) ? null : instance,
                this.arguments()
            );
        } catch (final ReflectiveOperationException failure) {
            throw new Unwrapped(failure).cause();
        }
    }

    private boolean fits(final Method candidate) {
        if (candidate.isVarArgs()) {
            return this.args.size() >= candidate.getParameterCount() - 1;
        }
        return candidate.getParameterCount() == this.args.size();
    }
}
