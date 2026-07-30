package ru.sprbut.m03;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Слайд 22 (СХЕМА 1): {@link Field} — узел карты Reflection API.
 * <p>
 * Field знает: свой тип, свой <i>обобщённый</i> тип, класс-владелец,
 * модификаторы, аннотации, а также умеет читать и писать значение.
 */
public final class FieldApi {

    private FieldApi() {
    }

    /** Сырой тип поля после стирания: {@code List} для {@code List<String>}. */
    public static Class<?> rawType(Field field) {
        return field.getType();
    }

    /**
     * Обобщённый тип поля. В отличие от {@code getType()}, дженерики здесь сохранены —
     * информация о них лежит в атрибуте {@code Signature} класс-файла, а не стирается.
     */
    public static Type genericType(Field field) {
        return field.getGenericType();
    }

    /**
     * Фактические типы-аргументы дженерика: для {@code Map<String, BigDecimal>}
     * вернёт {@code [String, BigDecimal]}. На этом стоит разбор типов в Jackson и Spring.
     */
    public static List<String> typeArguments(Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType parameterized) {
            return Arrays.stream(parameterized.getActualTypeArguments())
                    .map(FieldApi::typeName)
                    .toList();
        }
        return List.of();
    }

    /** Класс, в котором поле объявлено — не обязательно тот, у которого мы его спросили. */
    public static Class<?> owner(Field field) {
        return field.getDeclaringClass();
    }

    /** Является ли тип поля примитивом — важно при конвертации значений. */
    public static boolean isPrimitive(Field field) {
        return field.getType().isPrimitive();
    }

    /** У примитивов есть тип-обёртка; для ссылочных типов возвращаем их самих. */
    public static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "boolean" -> Boolean.class;
            default -> Void.class;
        };
    }

    private static String typeName(Type type) {
        return type instanceof Class<?> c ? c.getSimpleName() : type.getTypeName();
    }
}
