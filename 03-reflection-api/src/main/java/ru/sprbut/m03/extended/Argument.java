/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Строка из команды, превращённая в объект нужного параметру типа.
 *
 * <p>Тип берётся из {@code Parameter#getType()} — решение принимается по метаданным,
 * а не по внешнему знанию. Ровно так Spring MVC конвертирует параметры запроса
 * в аргументы метода контроллера.</p>
 *
 * @since 1.0
 */
public final class Argument {

    /**
     * Исходное значение.
     */
    private final String raw;

    /**
     * Целевой объект.
     */
    private final Class<?> target;

    /**
     * Основной конструктор.
     * @param raw Исходное значение
     * @param target Целевой объект
     */
    public Argument(final String raw, final Class<?> target) {
        this.raw = raw;
        this.target = target;
    }

    /**
     * Значение нужного типа.
     * @return Значение нужного типа
     */
    public Object value() {
        if ("null".equals(this.raw)) {
            return this.empty();
        }
        if (this.target.isEnum()) {
            return this.constant();
        }
        final Function<String, Object> rule = Convertible.RULES.get(this.target);
        if (rule == null) {
            throw new IllegalArgumentException(
                "Нет конвертера для типа " + this.target.getSimpleName()
            );
        }
        try {
            return rule.apply(this.raw);
        } catch (final RuntimeException malformed) {
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
                    + Arrays.toString(                        this.target.getEnumConstants()
)
            ));
    }
}
