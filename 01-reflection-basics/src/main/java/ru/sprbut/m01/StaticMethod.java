package ru.sprbut.m01;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Слайд 8: вызов статического метода — экземпляр не нужен.
 * <p>
 * Отличие от {@link ObjectMethod} ровно то же, что у статического поля:
 * вместо объекта рефлексии передаётся {@code null}.
 */
public final class StaticMethod {

    private final Class<?> type;

    private final String name;

    private final Class<?>[] parameters;

    public StaticMethod(Class<?> type, String name, Class<?>... parameters) {
        this.type = type;
        this.name = name;
        this.parameters = parameters.clone();
    }

    /**
     * Вызывает статический метод, игнорируя модификатор доступа.
     */
    public Object call(Object... args) {
        Method method = new Declared(this.type).method(this.name, this.parameters);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Нет доступа к методу " + this.name, denied);
        } catch (InvocationTargetException wrapped) {
            throw new Unwrapped(wrapped).cause();
        }
    }
}
