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
// приём захвата типа держится на анонимном подклассе: абстрактных методов
// у него нет и быть не должно, вся работа делается в конструкторе
@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
})
public abstract class TypeToken<T> {

    /**
     * Пойманный параметр типа.
     */
    private final Type captured;

    /**
     * Конструктор ловит параметр типа из объявления подкласса.
     * @checkstyle ConstructorsCodeFreeCheck (12 lines)
     */
    protected TypeToken() {
        final Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterized)) {
            throw new IllegalStateException(
                String.format(
                    "Тип не параметризован: %s, вероятно, забыты фигурные скобки подкласса",
                    superclass
                )
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
