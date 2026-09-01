/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Конструктор, выбранный под заданное число аргументов.
 *
 * <p>Отбор двухступенчатый: сначала по арности, затем по тому, умеет ли движок
 * построить значения нужных типов. Второй шаг важнее, чем кажется — без него
 * конструктор с параметром-коллекцией выигрывал бы у пригодного и падал позже,
 * уже на конвертации.</p>
 *
 * @since 1.0
 */
public final class ChosenConstructor {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Значение {@code arity}.
     */
    private final int arity;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param arity Значение {@code arity}
     */
    public ChosenConstructor(final Class<?> type, final int arity) {
        this.type = type;
        this.arity = arity;
    }

    /**
     * Подходящий конструктор, самый доступный из равных.
     * @return Подходящий конструктор, самый доступный из равных
     */
    public Constructor<?> constructor() {
        if (Modifier.isAbstract(this.type.getModifiers()) || this.type.isInterface()) {
            throw new IllegalArgumentException(
                String.format(
                    "Нельзя создать экземпляр %s: это абстрактный тип",
                    this.type.getSimpleName()
                )
            );
        }
        return Arrays.stream(this.type.getDeclaredConstructors())
            .filter(candidate -> candidate.getParameterCount() == this.arity)
            .filter(this::fillable)
            .min(Comparator.comparingInt(candidate -> new AccessRank(candidate).value()))
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format(
                        "У %s нет пригодного конструктора с %s аргументами",
                        this.type.getSimpleName(), this.arity
                    )
                )
            );
    }

    /**
     * Описание конструктора для отчёта.
     * @return Описание конструктора для отчёта
     */
    public String text() {
        final Constructor<?> chosen = this.constructor();
        return String.format(
            "%s(%s)",
            chosen.getDeclaringClass().getSimpleName(),
            String.join(
                ", ",
                Arrays.stream(chosen.getParameterTypes()).map(Class::getSimpleName).toList()
            )
        );
    }

    /**
     * Созданный этим конструктором объект.
     * @param args Аргументы
     * @return Созданный этим конструктором объект
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object instance(final List<String> args) {
        final Constructor<?> chosen = this.constructor();
        final Class<?>[] parameters = chosen.getParameterTypes();
        final Object[] values = new Object[parameters.length];
        for (int index = 0; index < parameters.length; index += 1) {
            values[index] = new Argument(args.get(index), parameters[index]).value();
        }
        chosen.setAccessible(true);
        try {
            return chosen.newInstance(values);
        } catch (final ReflectiveOperationException failure) {
            throw new Unwrapped(failure).cause();
        }
    }

    // @checkstyle NonStaticMethodCheck (3 lines)
    private boolean fillable(final Constructor<?> candidate) {
        return Arrays.stream(candidate.getParameterTypes())
            .allMatch(parameter -> new Convertible(parameter).yes());
    }
}
