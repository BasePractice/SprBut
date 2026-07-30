package ru.sprbut.m03.extended;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Строка из команды, превращённая в объект нужного параметру типа.
 * <p>
 * Тип берётся из {@code Parameter#getType()} — решение принимается по метаданным,
 * а не по внешнему знанию. Ровно так Spring MVC конвертирует параметры запроса
 * в аргументы метода контроллера.
 */
public final class Argument {

    private final String raw;

    private final Class<?> target;

    public Argument(String raw, Class<?> target) {
        this.raw = raw;
        this.target = target;
    }

    /**
     * Значение нужного типа.
     */
    public Object value() {
        if ("null".equals(this.raw)) {
            return empty();
        }
        if (this.target.isEnum()) {
            return constant();
        }
        Function<String, Object> rule = Convertible.RULES.get(this.target);
        if (rule == null) {
            throw new IllegalArgumentException(
                "Нет конвертера для типа " + this.target.getSimpleName()
            );
        }
        try {
            return rule.apply(this.raw);
        } catch (RuntimeException malformed) {
            throw new IllegalArgumentException(
                "Значение '" + this.raw + "' не приводится к " + this.target.getSimpleName(),
                malformed
            );
        }
    }

    private Object empty() {
        if (this.target.isPrimitive()) {
            throw new IllegalArgumentException(
                "null нельзя передать в примитивный параметр " + this.target.getSimpleName()
            );
        }
        return null;
    }

    private Object constant() {
        return Arrays.stream(this.target.getEnumConstants())
            .filter(candidate -> ((Enum<?>) candidate).name().equalsIgnoreCase(this.raw))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "'" + this.raw + "' не входит в "
                    + Arrays.toString(this.target.getEnumConstants())
            ));
    }
}
