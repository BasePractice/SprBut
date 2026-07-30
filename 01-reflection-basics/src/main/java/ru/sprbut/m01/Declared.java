package ru.sprbut.m01;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Поиск объявленного члена с подъёмом по иерархии наследования.
 * <p>
 * {@code getDeclaredField} и {@code getDeclaredMethod} смотрят только в самом
 * классе. Родителей приходится обходить вручную — и этот цикл в том или ином
 * виде написан в каждом фреймворке, работающем с рефлексией.
 */
public final class Declared {

    private final Class<?> type;

    public Declared(Class<?> type) {
        this.type = type;
    }

    /**
     * Поле с указанным именем, объявленное в классе или любом из его родителей.
     */
    public Field field(String name) {
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException absent) {
                continue;
            }
        }
        throw new IllegalArgumentException(
            "Поле '" + name + "' не найдено в " + this.type.getName()
        );
    }

    /**
     * Метод с указанным именем и сигнатурой, объявленный в классе или родителе.
     */
    public Method method(String name, Class<?>... parameters) {
        for (Class<?> current = this.type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException absent) {
                continue;
            }
        }
        throw new IllegalArgumentException(
            "Метод '" + name + "' не найден в " + this.type.getName()
        );
    }
}
