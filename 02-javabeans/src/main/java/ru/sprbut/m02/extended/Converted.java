/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.extended;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Строка из конфигурации, приведённая к типу свойства.
 *
 * <p>Конфигурация всегда приходит текстом — из файла, окружения или командной
 * строки. Превращение текста в {@code int}, {@code BigDecimal} или enum —
 * отдельная работа, которую в Spring делает {@code ConversionService};
 * здесь она сжата до таблицы конвертеров.</p>
 *
 * <p>Отказ немедленный и с контекстом: молча подставить {@code null} или ноль —
 * худшее, что может сделать биндер конфигурации.</p>
 *
 * @since 1.0
 */
public final class Converted {

    /**
     * Значение {@code RULES}.
     */
    private static final Map<Class<?>, Function<String, Object>> RULES = Map.ofEntries(
        Map.entry(String.class, raw -> raw),
        Map.entry(int.class, Integer::parseInt),
        Map.entry(Integer.class, Integer::valueOf),
        Map.entry(long.class, Long::parseLong),
        Map.entry(Long.class, Long::valueOf),
        Map.entry(double.class, Double::parseDouble),
        Map.entry(Double.class, Double::valueOf),
        Map.entry(boolean.class, Boolean::parseBoolean),
        Map.entry(Boolean.class, Boolean::valueOf),
        Map.entry(BigDecimal.class, BigDecimal::new),
        Map.entry(LocalDate.class, LocalDate::parse),
        Map.entry(UUID.class, UUID::fromString)
    );

    /**
     * Исходное значение.
     */
    private final String raw;

    /**
     * Целевой объект.
     */
    private final Class<?> target;

    /**
     * Имя свойства.
     */
    private final String property;

    /**
     * Основной конструктор.
     * @param raw Исходное значение
     * @param target Целевой объект
     * @param property Имя свойства
     */
    public Converted(final String raw, final Class<?> target, final String property) {
        this.raw = raw;
        this.target = target;
        this.property = property;
    }

    /**
     * Значение нужного типа.
     * @return Значение нужного типа
     */
    @SuppressWarnings("PMD.AvoidDirectAccessToStaticFields")
    public Object value() {
        if (this.target.isEnum()) {
            return this.constant();
        }
        final Function<String, Object> rule = RULES.get(this.target);
        if (rule == null) {
            throw new IllegalArgumentException(
                "Свойство '" + this.property + "': нет конвертера для типа "
                    + this.target.getSimpleName()
            );
        }
        try {
            return rule.apply(this.raw);
        } catch (final RuntimeException malformed) {
            throw new IllegalArgumentException(
                "Свойство '" + this.property + "': значение '" + this.raw
                    + "' не приводится к " + this.target.getSimpleName(),
                malformed
            );
        }
    }

    private Object constant() {
        return Arrays.stream(this.target.getEnumConstants())
            .filter(candidate -> ((Enum<?>) candidate).name().equalsIgnoreCase(this.raw))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Свойство '" + this.property + "': '" + this.raw + "' не входит в "
                    + Arrays.toString(this.target.getEnumConstants()
)
            ));
    }
}
