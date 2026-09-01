/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m05.extended;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Поля объекта, подлежащие проверке, — по всей цепочке наследования.
 *
 * <p>{@code @Inherited} здесь не помог бы при всём желании: оно действует только
 * на аннотации <b>классов</b> и никогда — на аннотации полей. Обходить иерархию
 * приходится руками, и по той же причине это делает Spring.</p>
 *
 * @since 1.0
 */
public final class ConstrainedFields {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public ConstrainedFields(final Class<?> type) {
        this.type = type;
    }

    /**
     * Поля класса и всех его предков, кроме статических и синтетических.
     * @return Поля класса и всех его предков, кроме статических и синтетических
     */
    public List<Field> list() {
        final List<Field> found = new ArrayList<>();
        for (Class<?> current = this.type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                    found.add(field);
                }
            }
        }
        return List.copyOf(found);
    }
}
