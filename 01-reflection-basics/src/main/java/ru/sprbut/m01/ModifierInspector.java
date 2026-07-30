package ru.sprbut.m01;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 6: «Определить модификаторы доступа полей, методов».
 * <p>
 * {@link Modifier} — это набор битовых флагов. {@code getModifiers()} возвращает
 * int, а статические методы {@code Modifier.isXxx} его расшифровывают.
 */
public final class ModifierInspector {

    private ModifierInspector() {
    }

    /**
     * Человекочитаемое описание модификаторов поля: {@code "private final"}.
     */
    public static String describe(Field field) {
        return Modifier.toString(field.getModifiers());
    }

    public static String describe(Method method) {
        return Modifier.toString(method.getModifiers());
    }

    /**
     * Имена полей, объявленных прямо в этом классе (без унаследованных).
     * {@code getDeclaredFields()} видит private, {@code getFields()} — только public.
     */
    public static List<String> declaredFieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .toList();
    }

    /**
     * Только публичные поля — включая унаследованные от родителей.
     */
    public static List<String> publicFieldNames(Class<?> type) {
        return Arrays.stream(type.getFields())
                .map(Field::getName)
                .toList();
    }

    public static List<String> privateFieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> Modifier.isPrivate(f.getModifiers()))
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .toList();
    }

    /**
     * Статические поля — их значение читается без экземпляра ({@code get(null)}).
     */
    public static List<String> staticFieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .toList();
    }

    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    public static List<String> declaredMethodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> !m.isSynthetic())
                .map(Method::getName)
                .sorted()
                .toList();
    }
}
