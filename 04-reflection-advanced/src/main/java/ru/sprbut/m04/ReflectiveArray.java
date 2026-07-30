package ru.sprbut.m04;

import java.lang.reflect.Array;

/**
 * Слайд 36: массив с типом элемента, известным только в runtime.
 * <p>
 * Обычный {@code new T[n]} в Java невозможен из-за стирания — остаётся
 * {@link Array#newInstance}. Ровно этим пользуется каждый десериализатор,
 * которому нужно построить массив нужного типа по метаданным.
 */
public final class ReflectiveArray {

    private final Class<?> component;

    public ReflectiveArray(Class<?> component) {
        this.component = component;
    }

    /**
     * Одномерный массив.
     */
    public Object single(int length) {
        return Array.newInstance(this.component, length);
    }

    /**
     * Многомерный массив создаётся тем же API — просто размеров больше одного.
     */
    public Object matrix(int rows, int columns) {
        return Array.newInstance(this.component, rows, columns);
    }
}
