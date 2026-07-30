package ru.sprbut.m03;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 26–27 (СХЕМА 1): {@link Class} — центр карты Reflection API.
 * <p>
 * Всё остальное — {@code Field}, {@code Method}, {@code Constructor} — добывается
 * из него. Сам {@code Class} при этом отвечает и на вопросы о природе типа:
 * примитив, массив, enum, интерфейс, record, вложенный класс.
 */
public final class ClassApi {

    private ClassApi() {
    }

    public static List<String> declaredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .sorted()
                .toList();
    }

    public static List<String> declaredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> !m.isSynthetic())
                .map(Method::getName)
                .sorted()
                .distinct()
                .toList();
    }

    public static int declaredConstructorCount(Class<?> type) {
        return type.getDeclaredConstructors().length;
    }

    /** Тип элемента массива: {@code String} для {@code String[]}, {@code null} иначе. */
    public static Class<?> componentType(Class<?> type) {
        return type.getComponentType();
    }

    /** Класс, внутри которого объявлен вложенный тип. */
    public static Class<?> enclosingClass(Class<?> type) {
        return type.getEnclosingClass();
    }

    /**
     * Категория типа одним словом. Проверки нужно делать именно в этом порядке:
     * например, enum одновременно является и классом.
     */
    public static String kindOf(Class<?> type) {
        if (type.isPrimitive()) {
            return "primitive";
        }
        if (type.isArray()) {
            return "array";
        }
        if (type.isEnum()) {
            return "enum";
        }
        if (type.isAnnotation()) {
            // проверяем ДО isInterface: аннотация — тоже интерфейс
            return "annotation";
        }
        if (type.isInterface()) {
            return "interface";
        }
        if (type.isRecord()) {
            return "record";
        }
        return "class";
    }

    /** Иерархия наследования снизу вверх, до {@code Object} включительно. */
    public static List<String> superChain(Class<?> type) {
        List<String> chain = new java.util.ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            chain.add(c.getSimpleName());
        }
        return List.copyOf(chain);
    }

    /**
     * Все интерфейсы, включая унаследованные — то, что фреймворки используют,
     * чтобы решить, подходит ли бин под тип зависимости.
     */
    public static List<String> allInterfaces(Class<?> type) {
        java.util.Set<String> result = new java.util.TreeSet<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            collectInterfaces(c, result);
        }
        return List.copyOf(result);
    }

    private static void collectInterfaces(Class<?> type, java.util.Set<String> sink) {
        for (Class<?> iface : type.getInterfaces()) {
            if (sink.add(iface.getSimpleName())) {
                collectInterfaces(iface, sink);
            }
        }
    }

    /**
     * {@code isAssignableFrom} читается наоборот, чем кажется:
     * {@code Number.class.isAssignableFrom(Integer.class)} — истина.
     */
    public static boolean canHold(Class<?> declaredType, Class<?> actualType) {
        return declaredType.isAssignableFrom(actualType);
    }

    /** Компоненты record — отдельная сущность API, появившаяся в Java 16. */
    public static List<String> recordComponents(Class<?> type) {
        if (!type.isRecord()) {
            return List.of();
        }
        return Arrays.stream(type.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();
    }

    /** Константы enum в порядке объявления. */
    public static List<String> enumConstants(Class<?> type) {
        Object[] constants = type.getEnumConstants();
        if (constants == null) {
            return List.of();
        }
        return Arrays.stream(constants).map(String::valueOf).toList();
    }

    /**
     * Массив создаётся не конструктором, а фабрикой {@code java.lang.reflect.Array}.
     * Это отдельная ветка API, которую легко упустить.
     */
    public static Object newArray(Class<?> componentType, int length) {
        return java.lang.reflect.Array.newInstance(componentType, length);
    }

    public static Constructor<?>[] constructors(Class<?> type) {
        return type.getDeclaredConstructors();
    }
}
