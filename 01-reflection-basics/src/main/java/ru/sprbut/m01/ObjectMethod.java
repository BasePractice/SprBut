package ru.sprbut.m01;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Слайд 8: «Вызвать методы объекта, в том числе с модификатором доступа private».
 * <p>
 * Важная деталь: если сам метод бросил исключение, рефлексия заворачивает его
 * в {@link InvocationTargetException}. Настоящую причину надо доставать
 * через {@code getCause()} — иначе стектрейс становится нечитаемым, а обработка
 * ошибок начинает ловить не то, что бросали.
 */
public final class ObjectMethod {

    private final Object target;

    private final String name;

    private final Class<?>[] parameters;

    public ObjectMethod(Object target, String name, Class<?>... parameters) {
        this.target = target;
        this.name = name;
        this.parameters = parameters.clone();
    }

    /**
     * Вызывает метод, игнорируя модификатор доступа.
     */
    public Object call(Object... args) {
        Method method = new Declared(this.target.getClass()).method(this.name, this.parameters);
        method.setAccessible(true);
        try {
            return method.invoke(this.target, args);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException("Нет доступа к методу " + this.name, denied);
        } catch (InvocationTargetException wrapped) {
            throw new Unwrapped(wrapped).cause();
        }
    }
}
