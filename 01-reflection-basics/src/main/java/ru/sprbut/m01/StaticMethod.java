/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Слайд 8: вызов статического метода — экземпляр не нужен.
 *
 * <p>Отличие от {@link ObjectMethod} ровно то же, что у статического поля:
 * вместо объекта рефлексии передаётся {@code null}.</p>
 *
 * @since 1.0
 */
public final class StaticMethod {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Имя.
     */
    private final String name;

    /**
     * Типы параметров.
     */
    private final Class<?>[] parameters;

    /**
     * Основной конструктор.
     * @param type Тип
     * @param name Имя
     * @param parameters Типы параметров
     */
    public StaticMethod(final Class<?> type, final String name, final Class<?>... parameters) {
        this.type = type;
        this.name = name;
        this.parameters = parameters.clone();
    }

    /**
     * Вызывает статический метод, игнорируя модификатор доступа.
     * @param args Аргументы
     * @return Вызывает статический метод, игнорируя модификатор доступа
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object call(final Object... args) {
        final Method method = new Declared(this.type).method(this.name, this.parameters);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Нет доступа к методу %s", this.name), denied
            );
        } catch (final InvocationTargetException wrapped) {
            throw new Unwrapped(wrapped).cause();
        }
    }
}
