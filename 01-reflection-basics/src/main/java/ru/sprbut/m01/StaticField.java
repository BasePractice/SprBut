package ru.sprbut.m01;

import java.lang.reflect.Field;

/**
 * Слайд 7: статическое поле читается без экземпляра.
 * <p>
 * Единственное отличие от {@link ObjectField} — {@code get(null)}: у статического
 * поля нет владельца, и передавать рефлексии некого.
 */
public final class StaticField {

    private final Class<?> type;

    private final String name;

    public StaticField(Class<?> type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Значение статического поля.
     */
    public Object value() {
        Field field = new Declared(this.type).field(this.name);
        field.setAccessible(true);
        try {
            return field.get(null);
        } catch (IllegalAccessException denied) {
            throw new IllegalStateException(
                "Не удалось прочитать статическое поле " + this.name, denied
            );
        }
    }
}
