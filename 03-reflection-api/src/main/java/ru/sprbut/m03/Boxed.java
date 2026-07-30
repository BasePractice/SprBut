package ru.sprbut.m03;

/**
 * Тип с примитивом, заменённым на его обёртку.
 * <p>
 * Рефлексия объявляет параметр как {@code int}, а аргументом всегда приходит
 * {@code Integer}: в массиве {@code Object[]} примитивов не бывает. Без этой
 * замены любая проверка совместимости типов вернула бы ложь.
 */
public final class Boxed {

    private final Class<?> type;

    public Boxed(Class<?> type) {
        this.type = type;
    }

    /**
     * Тип-обёртка для примитива; для ссылочного типа — он сам.
     */
    public Class<?> type() {
        if (!this.type.isPrimitive()) {
            return this.type;
        }
        return switch (this.type.getName()) {
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
}
