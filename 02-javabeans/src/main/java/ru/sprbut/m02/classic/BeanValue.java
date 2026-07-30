package ru.sprbut.m02.classic;

import java.beans.PropertyDescriptor;

/**
 * Одно свойство одного бина, доступное по имени.
 * <p>
 * Читается и пишется без знания класса на этапе компиляции — именно так
 * контейнер заполняет объект из properties или yaml.
 */
public final class BeanValue {

    private final Object bean;

    private final String property;

    public BeanValue(Object bean, String property) {
        this.bean = bean;
        this.property = property;
    }

    /**
     * Значение свойства.
     */
    public Object value() {
        PropertyDescriptor described = described();
        if (described.getReadMethod() == null) {
            throw new IllegalArgumentException(
                "Свойство '" + this.property + "' недоступно на чтение"
            );
        }
        return new Invoked(described.getReadMethod(), this.bean).value();
    }

    /**
     * Записывает значение через сеттер.
     */
    public void assign(Object value) {
        PropertyDescriptor described = described();
        if (described.getWriteMethod() == null) {
            throw new IllegalArgumentException(
                "Свойство '" + this.property + "' доступно только на чтение"
            );
        }
        new Invoked(described.getWriteMethod(), this.bean).value(value);
    }

    private PropertyDescriptor described() {
        return new Introspected(this.bean.getClass())
            .descriptor(this.property)
            .orElseThrow(
                () -> new IllegalArgumentException("Нет свойства '" + this.property + "'")
            );
    }
}
