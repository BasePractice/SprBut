package ru.sprbut.m01;

import java.util.Arrays;
import java.util.List;

/**
 * Слайды 3–5: «Механизм работы с метаданными объектов в runtime»,
 * «Позволяет узнать имя класса объекта».
 * <p>
 * Три способа получить {@link Class} и то, что из него сразу читается:
 * имена, иерархия, интерфейсы.
 */
public final class ClassMetadata {

    private ClassMetadata() {
    }

    /**
     * Полное имя класса вместе с пакетом: {@code ru.sprbut.m01.model.Account}.
     */
    public static String fullName(Object target) {
        return target.getClass().getName();
    }

    /**
     * Короткое имя без пакета: {@code Account}.
     */
    public static String simpleName(Object target) {
        return target.getClass().getSimpleName();
    }

    /**
     * Имя пакета, в котором объявлен класс объекта.
     */
    public static String packageName(Object target) {
        return target.getClass().getPackageName();
    }

    /**
     * Цепочка наследования от класса объекта до {@link Object} включительно.
     * Именно так фреймворки ищут аннотации и поля в родителях.
     */
    public static List<String> hierarchy(Object target) {
        return hierarchyOf(target.getClass());
    }

    public static List<String> hierarchyOf(Class<?> type) {
        List<String> names = new java.util.ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            names.add(c.getSimpleName());
        }
        return List.copyOf(names);
    }

    /**
     * Интерфейсы, которые класс реализует напрямую.
     */
    public static List<String> directInterfaces(Class<?> type) {
        return Arrays.stream(type.getInterfaces())
                .map(Class::getSimpleName)
                .toList();
    }

    /**
     * Слайд 4 (Reflection: детали, забегая вперёд): {@code Class.forName()} —
     * загрузка класса по строковому имени. Так работает чтение конфигов,
     * где имя класса лежит в текстовом файле.
     */
    public static Class<?> byName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * Признаки, по которым фреймворки решают, можно ли инстанцировать тип.
     */
    public static boolean isInstantiable(Class<?> type) {
        return !type.isInterface()
                && !java.lang.reflect.Modifier.isAbstract(type.getModifiers())
                && !type.isPrimitive()
                && !type.isEnum()
                && !type.isArray();
    }
}
