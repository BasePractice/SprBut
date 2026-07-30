package ru.sprbut.m10.lombok;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Что Lombok на самом деле дописал в класс.
 * <p>
 * Смысл упражнения в том, что рефлексия видит <b>байткод</b>, а не исходники.
 * В исходниках у класса с {@code @Data} нет ни одного метода — в байткоде
 * их дюжина. Это самый прямой способ убедиться, что annotation processor
 * не «магия IDE», а настоящая генерация кода на этапе компиляции.
 */
public final class Generated {

    private final Class<?> type;

    public Generated(Class<?> type) {
        this.type = type;
    }

    /**
     * Публичные методы, реально существующие в байткоде.
     */
    public List<String> methods() {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(method -> !method.isSynthetic())
            .map(Method::getName)
            .sorted()
            .distinct()
            .toList();
    }

    /**
     * Арности конструкторов — по ним видно {@code @NoArgsConstructor}
     * и {@code @AllArgsConstructor}.
     */
    public List<Integer> constructors() {
        return Arrays.stream(this.type.getDeclaredConstructors())
            .map(Constructor::getParameterCount)
            .sorted()
            .toList();
    }

    /**
     * Все ли поля финальны — признак {@code @Value}.
     */
    public boolean immutable() {
        return Arrays.stream(this.type.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .allMatch(field -> Modifier.isFinal(field.getModifiers()));
    }

    /**
     * Есть ли хотя бы один сеттер.
     */
    public boolean mutable() {
        return methods().stream().anyMatch(name -> name.startsWith("set"));
    }

    /**
     * Уровень доступа метода — {@code @Setter(AccessLevel.PROTECTED)} виден отсюда.
     */
    public String access(String method) {
        return Arrays.stream(this.type.getDeclaredMethods())
            .filter(candidate -> candidate.getName().equals(method))
            .findFirst()
            .map(candidate -> Modifier.toString(candidate.getModifiers()))
            .orElseThrow(() -> new IllegalArgumentException(
                "Нет метода '" + method + "' в " + this.type.getSimpleName()
            ));
    }

    /**
     * Подчиняется ли сгенерированный класс соглашению JavaBeans из модуля 02.
     */
    public boolean javaBean() {
        boolean constructible = Arrays.stream(this.type.getConstructors())
            .anyMatch(candidate -> candidate.getParameterCount() == 0);
        boolean accessible = Arrays.stream(this.type.getDeclaredFields())
            .filter(field -> !field.isSynthetic())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(Field::getName)
            .allMatch(this::readable);
        return constructible && accessible;
    }

    private boolean readable(String property) {
        String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        return methods().contains("get" + suffix) || methods().contains("is" + suffix);
    }
}
