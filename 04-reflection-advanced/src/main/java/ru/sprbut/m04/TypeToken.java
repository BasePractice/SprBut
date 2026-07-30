package ru.sprbut.m04;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Классический приём «type token»: анонимный подкласс сохраняет фактический
 * параметр типа в {@code getGenericSuperclass()}.
 * <p>
 * Использование: {@code new TypeToken<List<String>>() {}} — фигурные скобки
 * обязательны, именно они создают тот самый подкласс. Без них параметр
 * стёрся бы и ловить было бы нечего.
 * <p>
 * На этом построены {@code TypeReference} в Jackson и
 * {@code ParameterizedTypeReference} в Spring.
 */
public abstract class TypeToken<T> {

    private final Type captured;

    protected TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterized)) {
            throw new IllegalStateException(
                "Тип не параметризован: " + superclass
                    + " — вероятно, забыты фигурные скобки подкласса"
            );
        }
        this.captured = parameterized.getActualTypeArguments()[0];
    }

    /**
     * Пойманный параметр типа.
     */
    public final Type type() {
        return this.captured;
    }
}
