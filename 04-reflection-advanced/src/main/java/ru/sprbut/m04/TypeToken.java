/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m04;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Классический приём «type token»: анонимный подкласс сохраняет фактический
 * параметр типа в {@code getGenericSuperclass()}.
 *
 * <p>Использование: {@code new TypeToken<List<String>>() {}} — фигурные скобки
 * обязательны, именно они создают тот самый подкласс. Без них параметр
 * стёрся бы и ловить было бы нечего.</p>
 *
 * <p>На этом построены {@code TypeReference} в Jackson и
 * {@code ParameterizedTypeReference} в Spring.</p>
 *
  * @param <T> Параметр типа
 * @since 1.0
 */
public abstract class TypeToken<T> {

    /**
     * Значение {@code captured}.
     */
    private final Type captured;

    protected TypeToken() {
        final Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterized)) {
            throw new IllegalStateException(
                "Тип не параметризован: " + superclass
                    + " — вероятно, забыты фигурные скобки подкласса"
            );
        }
        this.captured = parameterized.getActualTypeArguments()[0];
    }

    /**
     * Пойманный параметр типа.
     * @return Пойманный параметр типа
     */
    public final Type type() {
        return this.captured;
    }
}
