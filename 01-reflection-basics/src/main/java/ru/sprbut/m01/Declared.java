/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Поиск объявленного члена с подъёмом по иерархии наследования.
 *
 * <p>{@code getDeclaredField} и {@code getDeclaredMethod} смотрят только в самом
 * классе. Родителей приходится обходить вручную — и этот цикл в том или ином
 * виде написан в каждом фреймворке, работающем с рефлексией.</p>
 *
 * @since 1.0
 */
public final class Declared {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Declared(final Class<?> type) {
        this.type = type;
    }

    /**
     * Поле с указанным именем, объявленное в классе или любом из его родителей.
     * @param name Имя
     * @return Поле с указанным именем, объявленное в классе или любом из его родителей
     */
    public Field field(final String name) {
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (final NoSuchFieldException absent) {
                continue;
            }
        }
        throw new IllegalArgumentException(
            String.format("Поле '%s' не найдено в %s", name, this.type.getName())
        );
    }

    /**
     * Метод с указанным именем и сигнатурой, объявленный в классе или родителе.
     * @param name Имя
     * @param parameters Типы параметров
     * @return Метод с указанным именем и сигнатурой, объявленный в классе или родителе
     */
    public Method method(final String name, final Class<?>... parameters) {
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredMethod(name, parameters);
            } catch (final NoSuchMethodException absent) {
                continue;
            }
        }
        throw new IllegalArgumentException(
            String.format("Метод '%s' не найден в %s", name, this.type.getName())
        );
    }
}
