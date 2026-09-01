/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Слайд 24 (СХЕМА 1): {@link Constructor} — точка входа любого IoC-контейнера.
 *
 * <p>Чтобы создать бин, контейнеру нужно выбрать конструктор и подобрать аргументы.
 * Логика выбора здесь — упрощённая версия того, что делает
 * {@code AutowiredAnnotationBeanPostProcessor} из модуля 12.</p>
 *
 * @since 1.0
 */
public final class Constructors {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Constructors(final Class<?> type) {
        this.type = type;
    }

    /**
     * Число параметров каждого публичного конструктора.
     * @return Число параметров каждого публичного конструктора
     */
    public List<Integer> publicArities() {
        return Arrays.stream(this.type.getConstructors())
            .map(Constructor::getParameterCount)
            .sorted()
            .toList();
    }

    /**
     * То же для всех объявленных конструкторов, включая protected и private.
     * @return То же для всех объявленных конструкторов, включая protected и private
     */
    public List<Integer> declaredArities() {
        return Arrays.stream(this.type.getDeclaredConstructors())
            .map(Constructor::getParameterCount)
            .sorted()
            .toList();
    }

    /**
     * Конструктор без параметров, если он есть.
     * @return Конструктор без параметров, если он есть
     */
    public Optional<Constructor<?>> noArg() {
        Optional<Constructor<?>> found;
        try {
            found = Optional.of(this.type.getDeclaredConstructor());
        } catch (final NoSuchMethodException absent) {
            found = Optional.empty();
        }
        return found;
    }

    /**
     * Конструктор с наибольшим числом параметров — «жадная» стратегия.
     * Так ведёт себя Jackson с {@code ParameterNamesModule}.
     * @return Конструктор с наибольшим числом параметров — «жадная» стратегия
     */
    public Optional<Constructor<?>> greediest() {
        return Arrays.stream(this.type.getConstructors())
            .max(Comparator.comparingInt(Constructor::getParameterCount));
    }

    /**
     * Конструктор, подходящий под конкретные аргументы: сравниваются количество
     * и совместимость типов с учётом автобоксинга. При равном соответствии
     * предпочитается более доступный.
     * @param args Аргументы
     * @return Конструктор, подходящий под конкретные аргументы
     */
    public Optional<Constructor<?>> matching(final Object... args) {
        return Arrays.stream(this.type.getDeclaredConstructors())
            .filter(candidate -> Constructors.fits(candidate.getParameterTypes(), args))
            .min(Comparator.comparingInt(Constructors::rank));
    }

    private static int rank(final Constructor<?> candidate) {
        final int rank;
        if (Modifier.isPublic(candidate.getModifiers())) {
            rank = 0;
        } else {
            rank = 1;
        }
        return rank;
    }

    @SuppressWarnings("PMD.UseVarargs")
    private static boolean fits(final Class<?>[] parameters, final Object[] args) {
        boolean suits = parameters.length == args.length;
        for (int index = 0; suits && index < parameters.length; index += 1) {
            if (args[index] == null) {
                suits = !parameters[index].isPrimitive();
            } else {
                suits = new Boxed(parameters[index]).type().isInstance(args[index]);
            }
        }
        return suits;
    }
}
