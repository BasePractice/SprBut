package ru.sprbut.m04;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 32: «Стирание типов: getGenericType, ParameterizedType».
 * <p>
 * Стирание не абсолютно. Из байткода исчезают типы <i>значений</i>, но
 * <i>объявления</i> — полей, параметров, возвращаемых значений, суперклассов —
 * сохраняют параметры типа в атрибуте {@code Signature}. Именно поэтому Jackson
 * умеет десериализовать в {@code List<Order>}, а Spring — понять, что бину
 * нужен {@code Repository<User>}.
 * <p>
 * {@link Type} — не один класс, а семейство: разбирать сигнатуру приходится,
 * спрашивая у каждого узла, какого он рода.
 */
public final class GenericType {

    private final Type type;

    public GenericType(Type type) {
        this.type = type;
    }

    /**
     * Параметры типа. Для {@code Map<String, List<Integer>>} — оба, вторым
     * останется вложенный {@code List<Integer>} целиком.
     */
    public List<String> arguments() {
        if (this.type instanceof ParameterizedType parameterized) {
            return Arrays.stream(parameterized.getActualTypeArguments())
                .map(Type::getTypeName)
                .toList();
        }
        return List.of();
    }

    /**
     * Разновидность узла в дереве типов — ключ к разбору произвольной сигнатуры.
     */
    public String kind() {
        if (this.type instanceof ParameterizedType) {
            return "ParameterizedType";
        }
        if (this.type instanceof WildcardType) {
            return "WildcardType";
        }
        if (this.type instanceof TypeVariable<?>) {
            return "TypeVariable";
        }
        if (this.type instanceof GenericArrayType) {
            return "GenericArrayType";
        }
        if (this.type instanceof Class<?>) {
            return "Class";
        }
        return this.type.getClass().getSimpleName();
    }

    /**
     * Верхняя граница: для {@code ? extends Number} — {@code Number},
     * для переменной типа — её объявленные границы.
     */
    public List<String> bounds() {
        if (this.type instanceof WildcardType wildcard) {
            return Arrays.stream(wildcard.getUpperBounds()).map(Type::getTypeName).toList();
        }
        if (this.type instanceof TypeVariable<?> variable) {
            return Arrays.stream(variable.getBounds()).map(Type::getTypeName).toList();
        }
        return List.of();
    }
}
