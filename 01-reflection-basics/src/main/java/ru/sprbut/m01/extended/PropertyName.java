package ru.sprbut.m01.extended;

import java.lang.reflect.Field;

/**
 * Имя ключа, под которым поле попадёт в JSON.
 * <p>
 * Значение {@link JsonProperty}, если оно задано, иначе имя самого поля.
 * Ровно так же устроены {@code @JsonProperty} в Jackson и {@code @SerializedName}
 * в Gson: аннотация не меняет поле, она меняет решение того, кто поле читает.
 */
public final class PropertyName {

    private final Field field;

    public PropertyName(Field field) {
        this.field = field;
    }

    /**
     * Имя ключа.
     */
    public String text() {
        JsonProperty renamed = this.field.getAnnotation(JsonProperty.class);
        if (renamed != null && !renamed.value().isBlank()) {
            return renamed.value();
        }
        return this.field.getName();
    }
}
