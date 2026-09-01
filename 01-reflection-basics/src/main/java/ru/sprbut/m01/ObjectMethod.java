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
 * Слайд 8: «Вызвать методы объекта, в том числе с модификатором доступа private».
 *
 * <p>Важная деталь: если сам метод бросил исключение, рефлексия заворачивает его
 * в {@link InvocationTargetException}. Настоящую причину надо доставать
 * через {@code getCause()} — иначе стектрейс становится нечитаемым, а обработка
 * ошибок начинает ловить не то, что бросали.</p>
 *
 * @since 1.0
 */
public final class ObjectMethod {

    /**
     * Целевой объект.
     */
    private final Object target;

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
     * @param target Целевой объект
     * @param name Имя
     * @param parameters Типы параметров
     */
    public ObjectMethod(final Object target, final String name, final Class<?>... parameters) {
        this.target = target;
        this.name = name;
        this.parameters = parameters.clone();
    }

    /**
     * Вызывает метод, игнорируя модификатор доступа.
     * @param args Аргументы
     * @return Вызывает метод, игнорируя модификатор доступа
     */
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public Object call(final Object... args) {
        final Method method = new Declared(this.target.getClass()).method(this.name, this.parameters);
        method.setAccessible(true);
        try {
            return method.invoke(this.target, args);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException("Нет доступа к методу " + this.name, denied);
        } catch (final InvocationTargetException wrapped) {
            throw new Unwrapped(wrapped).cause();
        }
    }
}
