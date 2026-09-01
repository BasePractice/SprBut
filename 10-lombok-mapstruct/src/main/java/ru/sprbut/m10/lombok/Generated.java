/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m10.lombok;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Что Lombok на самом деле дописал в класс.
 *
 * <p>Смысл упражнения в том, что рефлексия видит <b>байткод</b>, а не исходники.
 * В исходниках у класса с {@code @Data} нет ни одного метода — в байткоде
 * их дюжина. Это самый прямой способ убедиться, что annotation processor
 * не «магия IDE», а настоящая генерация кода на этапе компиляции.</p>
 *
 * @since 1.0
 */
public final class Generated {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Generated(final Class<?> type) {
        this.type = type;
    }

    /**
     * Публичные методы, реально существующие в байткоде.
     * @return Публичные методы, реально существующие в байткоде
     */
    public List<String> methods() {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(method -> !method.isSynthetic())
            .map(Method::getName)
            .sorted()
            .distinct()
            .toList();
    }

    /**
     * Арности конструкторов — по ним видно {@code @NoArgsConstructor}
     * и {@code @AllArgsConstructor}.
     * @return Арности конструкторов
     */
    public List<Integer> constructors() {
        return Arrays.stream(this.type.getDeclaredConstructors())
            .map(Constructor::getParameterCount)
            .sorted()
            .toList();
    }

    /**
     * Все ли поля финальны — признак {@code @Value}.
     * @return Все ли поля финальны — признак {@code @Value}
     */
    public boolean immutable() {
        return Arrays.stream(this.type.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .allMatch(field -> Modifier.isFinal(field.getModifiers()));
    }

    /**
     * Есть ли хотя бы один сеттер.
     * @return Есть ли хотя бы один сеттер
     */
    public boolean mutable() {
        return this.methods().stream().anyMatch(name -> name.startsWith("set"));
    }

    /**
     * Уровень доступа метода — {@code @Setter(AccessLevel.PROTECTED)} виден отсюда.
     * @param method Метод
     * @return Уровень доступа метода — {@code @Setter(AccessLevel.PROTECTED)} виден отсюда
     */
    public String access(final String method) {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(candidate -> candidate.getName().equals(method))
            .findFirst()
            .map(
                candidate -> Modifier.toString(
                    candidate.getModifiers()
                )
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format(
                        "Нет метода '%s' в %s", method, this.type.getSimpleName()
                    )
                )
            );
    }

    /**
     * Подчиняется ли сгенерированный класс соглашению JavaBeans из модуля 02.
     * @return Подчиняется ли сгенерированный класс соглашению JavaBeans из модуля 02
     */
    public boolean javaBean() {
        return Arrays.stream(this.type.getConstructors())
            .anyMatch(candidate -> candidate.getParameterCount() == 0)
            && Arrays.stream(this.type.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(Field::getName)
                .allMatch(this::readable);
    }

    private boolean readable(final String property) {
        final String suffix = String.format(
            "%s%s", Character.toUpperCase(property.charAt(0)), property.substring(1)
        );
        return this.methods().contains(String.format("get%s", suffix))
            || this.methods().contains(String.format("is%s", suffix));
    }
}
