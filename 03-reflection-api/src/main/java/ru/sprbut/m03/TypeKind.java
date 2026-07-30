package ru.sprbut.m03;

/**
 * Категория типа одним словом.
 * <p>
 * Порядок проверок здесь — не стилистика, а необходимость: аннотация является
 * интерфейсом, enum является классом. Перепутанный порядок молча выдаст
 * правдоподобный, но неверный ответ.
 */
public final class TypeKind {

    private final Class<?> type;

    public TypeKind(Class<?> type) {
        this.type = type;
    }

    /**
     * Одно из: primitive, array, enum, annotation, interface, record, class.
     */
    public String name() {
        if (this.type.isPrimitive()) {
            return "primitive";
        }
        if (this.type.isArray()) {
            return "array";
        }
        if (this.type.isEnum()) {
            return "enum";
        }
        if (this.type.isAnnotation()) {
            return "annotation";
        }
        if (this.type.isInterface()) {
            return "interface";
        }
        if (this.type.isRecord()) {
            return "record";
        }
        return "class";
    }
}
