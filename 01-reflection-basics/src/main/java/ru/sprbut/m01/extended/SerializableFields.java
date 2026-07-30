package ru.sprbut.m01.extended;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Поля класса, попадающие в JSON.
 * <p>
 * Отбор идёт по всей цепочке наследования, кроме статических, синтетических,
 * {@code transient} и помеченных {@link JsonIgnore}. Порядок — сначала поля
 * самого класса, затем родителей: иначе вывод зависел бы от того, где объявлено
 * поле, и тесты стали бы хрупкими.
 */
public final class SerializableFields {

    private final Class<?> type;

    public SerializableFields(Class<?> type) {
        this.type = type;
    }

    /**
     * Отобранные поля в порядке от потомка к предку.
     */
    public List<Field> list() {
        List<Field> selected = new ArrayList<>();
        for (Class<?> current = this.type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (serializable(field)) {
                    selected.add(field);
                }
            }
        }
        return List.copyOf(selected);
    }

    private boolean serializable(Field field) {
        return !field.isSynthetic()
            && !Modifier.isStatic(field.getModifiers())
            && !Modifier.isTransient(field.getModifiers())
            && !field.isAnnotationPresent(JsonIgnore.class);
    }
}
