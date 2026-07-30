package ru.sprbut.m01;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Слайд 8: «Вызвать методы объекта, в том числе с модификатором доступа private».
 * <p>
 * Важная деталь: если сам метод бросил исключение, рефлексия заворачивает его
 * в {@link InvocationTargetException}. Настоящую причину надо доставать
 * через {@code getCause()} — иначе стектрейс становится нечитаемым.
 */
public final class MethodInvoker {

    private MethodInvoker() {
    }

    /**
     * Вызывает метод по имени и типам параметров, игнорируя модификатор доступа.
     */
    public static Object invoke(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        Method method = findMethod(target.getClass(), methodName, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Нет доступа к методу " + methodName, e);
        } catch (InvocationTargetException e) {
            // Разворачиваем: наружу отдаём то, что реально бросил метод.
            throw asUnchecked(e.getCause());
        }
    }

    /**
     * Вызов статического метода: экземпляр не нужен, передаём {@code null}.
     */
    public static Object invokeStatic(Class<?> type, String methodName, Class<?>[] paramTypes, Object... args) {
        Method method = findMethod(type, methodName, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Нет доступа к методу " + methodName, e);
        } catch (InvocationTargetException e) {
            throw asUnchecked(e.getCause());
        }
    }

    public static Method findMethod(Class<?> type, String methodName, Class<?>... paramTypes) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ignored) {
                // ищем выше по иерархии
            }
        }
        throw new IllegalArgumentException("Метод '" + methodName + "' не найден в " + type.getName());
    }

    private static RuntimeException asUnchecked(Throwable cause) {
        if (cause instanceof RuntimeException re) {
            return re;
        }
        return new IllegalStateException(cause);
    }
}
