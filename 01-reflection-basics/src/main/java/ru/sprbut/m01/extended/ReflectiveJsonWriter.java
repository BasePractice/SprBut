package ru.sprbut.m01.extended;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <b>Расширенный пример модуля 01.</b>
 * <p>
 * Мини-сериализатор в JSON, написанный <i>исключительно</i> на рефлексии.
 * Собирает вместе всё, что перечислено на слайдах 3–10:
 * <ul>
 *   <li>получает {@code Class} объекта и поднимается по иерархии наследования;</li>
 *   <li>читает модификаторы, чтобы пропустить {@code static} и {@code transient};</li>
 *   <li>читает значения private-полей через {@code setAccessible(true)};</li>
 *   <li>читает аннотации {@link JsonProperty} и {@link JsonIgnore}.</li>
 * </ul>
 * Это ровно тот принцип, на котором построены Jackson, Gson и биндинг Spring:
 * поведение задаётся метаданными, а не написанным вручную кодом.
 */
public final class ReflectiveJsonWriter {

    private ReflectiveJsonWriter() {
    }

    public static String write(Object target) {
        if (target == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        writeValue(sb, target);
        return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof CharSequence || value instanceof Character || value instanceof Enum<?>) {
            sb.append('"').append(escape(value.toString())).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Collection<?> collection) {
            writeCollection(sb, collection);
        } else if (value.getClass().isArray()) {
            writeCollection(sb, toList(value));
        } else if (value instanceof Map<?, ?> map) {
            writeMap(sb, map);
        } else {
            writeObject(sb, value);
        }
    }

    private static void writeObject(StringBuilder sb, Object bean) {
        sb.append('{');
        boolean first = true;
        for (Field field : serializableFields(bean.getClass())) {
            field.setAccessible(true);
            Object fieldValue;
            try {
                fieldValue = field.get(bean);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Поле " + field.getName() + " недоступно", e);
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(propertyName(field))).append("\":");
            writeValue(sb, fieldValue);
        }
        sb.append('}');
    }

    private static void writeCollection(StringBuilder sb, Collection<?> collection) {
        sb.append('[');
        boolean first = true;
        for (Object element : collection) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeValue(sb, element);
        }
        sb.append(']');
    }

    private static void writeMap(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
            writeValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    /**
     * Поля, попадающие в JSON: все объявленные по всей цепочке наследования,
     * кроме статических, синтетических, transient и помеченных {@link JsonIgnore}.
     * <p>
     * Порядок: сначала поля самого класса, затем родителей — иначе вывод
     * зависел бы от того, где объявлено поле, и тесты стали бы хрупкими.
     */
    static List<Field> serializableFields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                int mods = field.getModifiers();
                if (field.isSynthetic() || Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                    continue;
                }
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Имя ключа: значение {@link JsonProperty}, если задано, иначе имя поля.
     */
    static String propertyName(Field field) {
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        if (annotation != null && !annotation.value().isBlank()) {
            return annotation.value();
        }
        return field.getName();
    }

    private static List<Object> toList(Object array) {
        int length = java.lang.reflect.Array.getLength(array);
        List<Object> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(java.lang.reflect.Array.get(array, i));
        }
        return list;
    }

    private static String escape(String raw) {
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
