package ru.sprbut.m04;

import java.lang.reflect.Array;

/**
 * Элементы массива, доступные без приведения типа.
 * <p>
 * {@code Object[]} здесь не подошёл бы: массив примитивов им не является,
 * и {@code int[]} пришлось бы обрабатывать отдельной веткой. {@link Array}
 * работает с любым массивом одинаково — за это и платят потерей типизации.
 */
public final class ArrayValue {

    private final Object array;

    public ArrayValue(Object array) {
        this.array = array;
    }

    /**
     * Элемент по индексу.
     */
    public Object element(int index) {
        return Array.get(this.array, index);
    }

    /**
     * Записывает элемент по индексу.
     */
    public void assign(int index, Object value) {
        Array.set(this.array, index, value);
    }

    /**
     * Длина массива.
     */
    public int length() {
        return Array.getLength(this.array);
    }
}
