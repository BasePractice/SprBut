/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 22 (СХЕМА 1): {@link Field} — узел карты Reflection API.
 *
 * <p>Тип поля в двух видах сразу. {@code getType()} отдаёт сырой тип после стирания,
 * {@code getGenericType()} — обобщённый: информация о дженериках не исчезает,
 * а лежит в атрибуте {@code Signature} класс-файла.</p>
 *
 * <p>Именно на этом различии стоит разбор типов в Jackson и Spring: чтобы понять,
 * что перед нами {@code List<Order>}, а не просто {@code List}, нужен второй метод.</p>
 *
 * @since 1.0
 */
public final class FieldType {

    /**
     * Поле.
     */
    private final Field field;

    /**
     * Основной конструктор.
     * @param field Поле
     */
    public FieldType(final Field field) {
        this.field = field;
    }

    /**
     * Сырой тип после стирания: {@code List} для {@code List<String>}.
     * @return Сырой тип после стирания: {@code List} для {@code List<String>}
     */
    public Class<?> raw() {
        return this.field.getType();
    }

    /**
     * Обобщённый тип, в котором дженерики сохранены.
     * @return Обобщённый тип, в котором дженерики сохранены
     */
    public Type generic() {
        return this.field.getGenericType();
    }

    /**
     * Фактические типы-аргументы: для {@code Map<String, BigDecimal>} — оба.
     * @return Фактические типы-аргументы: для {@code Map<String, BigDecimal>} — оба
     */
    public List<String> arguments() {
        if (this.field.getGenericType() instanceof ParameterizedType parameterized) {
            return Arrays.stream(parameterized.getActualTypeArguments())
                .map(this::name)
                .toList();
        }
        return List.of();
    }

    /**
     * Класс, в котором поле объявлено, — не обязательно тот, у которого его спросили.
     * @return Класс, в котором поле объявлено, — не обязательно тот, у которого его спросили
     */
    public Class<?> owner() {
        return this.field.getDeclaringClass();
    }

    /**
     * Примитивен ли тип поля — от этого зависит конвертация значений.
     * @return Примитивен ли тип поля — от этого зависит конвертация значений
     */
    public boolean primitive() {
        return this.field.getType().isPrimitive();
    }

    private String name(final Type type) {
        return type instanceof Class<?> known ? known.getSimpleName() : type.getTypeName();
    }
}
