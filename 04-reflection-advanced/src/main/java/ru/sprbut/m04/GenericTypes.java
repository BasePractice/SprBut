package ru.sprbut.m04;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
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
 * сохраняют параметры типа в атрибуте {@code Signature}. Именно поэтому
 * Jackson умеет десериализовать в {@code List<Order>}, а Spring — понять,
 * что бину нужен {@code Repository<User>}.
 */
public final class GenericTypes {

    private GenericTypes() {
    }

    /** Сырой тип: то, что осталось после стирания. */
    public static Class<?> rawTypeOf(Field field) {
        return field.getType();
    }

    /**
     * Параметры типа поля. Для {@code Map<String, List<Integer>>} вернёт
     * {@code ["java.lang.String", "java.util.List<java.lang.Integer>"]}.
     */
    public static List<String> typeArgumentsOf(Field field) {
        return typeArguments(field.getGenericType());
    }

    /** Параметры типа возвращаемого значения метода. */
    public static List<String> returnTypeArgumentsOf(Method method) {
        return typeArguments(method.getGenericReturnType());
    }

    private static List<String> typeArguments(Type type) {
        if (type instanceof ParameterizedType parameterized) {
            return Arrays.stream(parameterized.getActualTypeArguments())
                    .map(Type::getTypeName)
                    .toList();
        }
        return List.of();
    }

    /**
     * Разновидность узла в дереве типов. Это ключ к разбору произвольных сигнатур:
     * реализация {@link Type} определяет, как читать узел дальше.
     */
    public static String kindOf(Type type) {
        if (type instanceof ParameterizedType) {
            return "ParameterizedType";
        }
        if (type instanceof WildcardType) {
            return "WildcardType";
        }
        if (type instanceof TypeVariable<?>) {
            return "TypeVariable";
        }
        if (type instanceof GenericArrayType) {
            return "GenericArrayType";
        }
        if (type instanceof Class<?>) {
            return "Class";
        }
        return type.getClass().getSimpleName();
    }

    /**
     * Классический трюк «type token»: анонимный подкласс сохраняет фактический
     * параметр типа в {@code getGenericSuperclass()}. На нём построены
     * {@code TypeReference} в Jackson и {@code ParameterizedTypeReference} в Spring.
     */
    public static Type captureFromSubclass(Class<?> anonymousSubclass) {
        Type superclass = anonymousSubclass.getGenericSuperclass();
        if (superclass instanceof ParameterizedType parameterized) {
            return parameterized.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Тип не параметризован: " + superclass);
    }

    /** Верхняя граница wildcard: {@code ? extends Number} → {@code Number}. */
    public static Type upperBoundOf(WildcardType wildcard) {
        Type[] upper = wildcard.getUpperBounds();
        return upper.length > 0 ? upper[0] : Object.class;
    }

    /** Границы переменной типа: {@code <T extends Comparable<T>>}. */
    public static List<String> boundsOf(TypeVariable<?> variable) {
        return Arrays.stream(variable.getBounds()).map(Type::getTypeName).toList();
    }

    /**
     * Абстрактный «токен типа» — минимальная версия {@code TypeReference}.
     * Использование: {@code new TypeToken<List<String>>() {}}.
     */
    public abstract static class TypeToken<T> {

        private final Type captured;

        protected TypeToken() {
            this.captured = captureFromSubclass(getClass());
        }

        public Type type() {
            return captured;
        }
    }

    /** Носитель разнообразных обобщённых объявлений для тестов. */
    @SuppressWarnings("unused")
    public static class Holder<T extends Comparable<T>> {
        public List<String> names;
        public java.util.Map<String, List<Integer>> nested;
        public List<? extends Number> covariant;
        public T typeVariable;
        public T[] genericArray;
        public String plain;

        public List<T> produce() {
            return List.of();
        }
    }
}
