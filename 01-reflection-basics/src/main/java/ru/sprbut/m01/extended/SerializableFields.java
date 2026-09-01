/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01.extended;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Поля класса, попадающие в JSON.
 *
 * <p>Отбор идёт по всей цепочке наследования, кроме статических, синтетических,
 * {@code transient} и помеченных {@link JsonIgnore}. Порядок — сначала поля
 * самого класса, затем родителей: иначе вывод зависел бы от того, где объявлено
 * поле, и тесты стали бы хрупкими.</p>
 *
 * @since 1.0
 */
public final class SerializableFields {

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public SerializableFields(final Class<?> type) {
        this.type = type;
    }

    /**
     * Отобранные поля в порядке от потомка к предку.
     * @return Отобранные поля в порядке от потомка к предку
     */
    public List<Field> list() {
        final List<Field> selected = new ArrayList<>();
        for (Class<?> current = this.type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (final Field field : current.getDeclaredFields()) {
                if (this.serializable(field)) {
                    selected.add(field);
                }
            }
        }
        return List.copyOf(selected);
    }

    private static boolean serializable(final Field field) {
        return !field.isSynthetic()
            && !Modifier.isStatic(field.getModifiers())
            && !Modifier.isTransient(field.getModifiers())
            && !field.isAnnotationPresent(JsonIgnore.class);
    }
}
