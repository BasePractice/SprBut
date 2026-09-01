/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.beans.PropertyDescriptor;

/**
 * Одно свойство одного бина, доступное по имени.
 *
 * <p>Читается и пишется без знания класса на этапе компиляции — именно так
 * контейнер заполняет объект из properties или yaml.</p>
 *
 * @since 1.0
 */
public final class BeanValue {

    /**
     * Объект.
     */
    private final Object bean;

    /**
     * Имя свойства.
     */
    private final String property;

    /**
     * Основной конструктор.
     * @param bean Объект
     * @param property Имя свойства
     */
    public BeanValue(final Object bean, final String property) {
        this.bean = bean;
        this.property = property;
    }

    /**
     * Значение свойства.
     * @return Значение свойства
     */
    public Object value() {
        final PropertyDescriptor described = this.described();
        if (described.getReadMethod() == null) {
            throw new IllegalArgumentException(
                String.format("Свойство '%s' недоступно на чтение", this.property)
            );
        }
        return new Invoked(described.getReadMethod(), this.bean).value();
    }

    /**
     * Записывает значение через сеттер.
     * @param value Значение
     */
    public void assign(final Object value) {
        final PropertyDescriptor described = this.described();
        if (described.getWriteMethod() == null) {
            throw new IllegalArgumentException(
                String.format("Свойство '%s' доступно только на чтение", this.property)
            );
        }
        new Invoked(described.getWriteMethod(), this.bean).value(value);
    }

    private PropertyDescriptor described() {
        return new Introspected(this.bean.getClass())
            .descriptor(this.property)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format("Нет свойства '%s'", this.property)
                )
            );
    }
}
