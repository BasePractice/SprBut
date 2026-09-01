/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.extended;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Точки внедрения одного класса, найденные рефлексией.
 *
 * <p>Три аннотации проверяются вместе не для полноты: {@code @Autowired} — от Spring,
 * {@code @Inject} — стандарт JSR-330, {@code @Resource} — JSR-250, и контейнер
 * принимает все три. Аудит, знающий только одну, пропустил бы половину кода.</p>
 *
 * @since 1.0
 */
public final class InjectionPoints {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public InjectionPoints(final Class<?> type) {
        this.type = type;
    }

    /**
     * Конструктор, который выберет контейнер.
     *
     * <p>Правило Spring: если конструктор один, он и используется — аннотация
     * не нужна. Если их несколько, нужен явный {@code @Autowired}.</p>
     *
     * @return Конструктор, который выберет контейнер
     */
    public Constructor<?> constructor() {
        final Constructor<?>[] declared = this.type.getDeclaredConstructors();
        final Constructor<?> chosen;
        if (declared.length == 1) {
            chosen = declared[0];
        } else {
            chosen = Arrays.stream(declared)
                .filter(InjectionPoints::injected)
                .findFirst()
                .orElse(null);
        }
        return chosen;
    }

    /**
     * Поля, помеченные для внедрения, включая унаследованные.
     * @return Поля, помеченные для внедрения, включая унаследованные
     */
    public List<Field> fields() {
        final List<Field> found = new ArrayList<>(0);
        Class<?> current = this.type;
        while (current != null && current != Object.class) {
            Arrays.stream(current.getDeclaredFields())
                .filter(InjectionPoints::injected)
                .forEach(found::add);
            current = current.getSuperclass();
        }
        return List.copyOf(found);
    }

    /**
     * Сеттеры, помеченные для внедрения.
     * @return Сеттеры, помеченные для внедрения
     */
    public List<Method> setters() {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(method -> method.getParameterCount() == 1)
            .filter(InjectionPoints::injected)
            .sorted(Comparator.comparing(Method::getName))
            .toList();
    }

    /**
     * Все ли нестатические поля объявлены {@code final}.
     * @return Все ли нестатические поля объявлены {@code final}
     */
    public boolean immutable() {
        return Arrays.stream(this.type.getDeclaredFields())
            .filter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
            .allMatch(field -> Modifier.isFinal(field.getModifiers()));
    }

    private static boolean injected(final AnnotatedElement element) {
        return InjectionPoints.marked(element, Autowired.class)
            || InjectionPoints.marked(element, Inject.class)
            || InjectionPoints.marked(element, Resource.class);
    }

    private static boolean marked(
        final AnnotatedElement element, final Class<? extends Annotation> annotation
    ) {
        return element.isAnnotationPresent(annotation);
    }
}
