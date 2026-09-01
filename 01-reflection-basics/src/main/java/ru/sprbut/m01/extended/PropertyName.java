/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01.extended;

import java.lang.reflect.Field;

/**
 * Имя ключа, под которым поле попадёт в JSON.
 *
 * <p>Значение {@link JsonProperty}, если оно задано, иначе имя самого поля.
 * Ровно так же устроены {@code @JsonProperty} в Jackson и {@code @SerializedName}
 * в Gson: аннотация не меняет поле, она меняет решение того, кто поле читает.</p>
 *
 * @since 1.0
 */
public final class PropertyName {

    /**
     * Поле.
     */
    private final Field field;

    /**
     * Основной конструктор.
     * @param field Поле
     */
    public PropertyName(final Field field) {
        this.field = field;
    }

    /**
     * Имя ключа.
     * @return Имя ключа
     */
    public String text() {
        final JsonProperty renamed = this.field.getAnnotation(JsonProperty.class);
        if (renamed != null && !renamed.value().isBlank()) {
            return renamed.value();
        }
        return this.field.getName();
    }
}
