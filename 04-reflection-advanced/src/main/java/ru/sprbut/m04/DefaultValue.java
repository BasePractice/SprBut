package ru.sprbut.m04;

/**
 * Значение по умолчанию для типа.
 * <p>
 * Для ссылочного типа это {@code null}, для примитива — его собственный ноль.
 * Вернуть {@code null} вместо {@code int} нельзя: прокси упал бы на распаковке,
 * причём в месте, никак не связанном с настоящей причиной.
 */
public final class DefaultValue {

    private final Class<?> type;

    public DefaultValue(Class<?> type) {
        this.type = type;
    }

    /**
     * Значение по умолчанию.
     */
    public Object value() {
        if (!this.type.isPrimitive() || this.type == void.class) {
            return null;
        }
        if (this.type == boolean.class) {
            return false;
        }
        if (this.type == char.class) {
            return '\0';
        }
        if (this.type == long.class) {
            return 0L;
        }
        if (this.type == double.class) {
            return 0.0d;
        }
        if (this.type == float.class) {
            return 0.0f;
        }
        return 0;
    }
}
