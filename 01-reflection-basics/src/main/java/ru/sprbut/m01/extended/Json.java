/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01.extended;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <b>Расширенный пример модуля 01.</b>
 *
 * <p>Значение, представленное в JSON, — мини-сериализатор, написанный
 * <i>исключительно</i> на рефлексии. Собирает вместе всё, что перечислено
 * на слайдах 3–10:
 * <ul>
 * <li>получает {@code Class} объекта и поднимается по иерархии наследования;</li>
 * <li>читает модификаторы, чтобы пропустить {@code static} и {@code transient};</li>
 * <li>читает значения private-полей через {@code setAccessible(true)};</li>
 * <li>читает аннотации {@link JsonProperty} и {@link JsonIgnore}.</li>
 * </ul>
 * Это ровно тот принцип, на котором построены Jackson, Gson и биндинг Spring:
 * поведение задаётся метаданными, а не написанным вручную кодом.</p>
 *
 * <p>Вложенные объекты сериализуются тем же классом — рекурсия здесь выражена
 * композицией, а не отдельным методом обхода.</p>
 *
 * @since 1.0
 */
public final class Json {

    /**
     * Значение.
     */
    private final Object value;

    /**
     * Основной конструктор.
     * @param value Значение
     */
    public Json(final Object value) {
        this.value = value;
    }

    /**
     * Текст JSON для этого значения.
     * @return Текст JSON для этого значения
     */
    public String text() {
        final String json;
        if (this.value == null) {
            json = "null";
        } else if (this.value instanceof CharSequence
            || this.value instanceof Character
            || this.value instanceof Enum<?>) {
            json = String.format("\"%s\"", new Escaped(this.value.toString()).text());
        } else if (this.value instanceof Number || this.value instanceof Boolean) {
            json = this.value.toString();
        } else if (this.value instanceof Collection<?> items) {
            json = Json.array(items.stream());
        } else if (this.value.getClass().isArray()) {
            json = Json.array(
                IntStream.range(0, Array.getLength(this.value))
                    .mapToObj(index -> Array.get(this.value, index))
            );
        } else if (this.value instanceof Map<?, ?> entries) {
            json = entries.entrySet().stream()
                .map(entry -> Json.pair(String.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
        } else {
            json = this.object();
        }
        return json;
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    private Object read(final Field field) {
        field.setAccessible(true);
        try {
            return field.get(this.value);
        } catch (final IllegalAccessException denied) {
            throw new IllegalStateException(
                String.format("Поле %s недоступно", field.getName()), denied
            );
        }
    }

    private static String array(final Stream<?> items) {
        return items
            .map(item -> new Json(item).text())
            .collect(Collectors.joining(",", "[", "]"));
    }

    private static String pair(final String key, final Object nested) {
        return String.format("%s%s\":%s", '"', new Escaped(key).text(), new Json(nested).text());
    }

    private String object() {
        return new SerializableFields(this.value.getClass()).list().stream()
            .map(field -> Json.pair(new PropertyName(field).text(), this.read(field)))
            .collect(Collectors.joining(",", "{", "}"));
    }
}
