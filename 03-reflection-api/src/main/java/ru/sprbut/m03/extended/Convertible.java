/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m03.extended;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

/**
 * Типы, в которые движок команд умеет превращать строку.
 *
 * <p>Таблица одна на всех: по ней {@link Argument} конвертирует значение,
 * а {@link ChosenConstructor} заранее отсеивает конструкторы, чьи параметры
 * заполнить всё равно нечем.</p>
 *
 * @since 1.0
 */
public final class Convertible {

    /**
     * Значение {@code RULES}.
     */
    static final Map<Class<?>, Function<String, Object>> RULES = Map.ofEntries(
        Map.entry(String.class, raw -> raw),
        Map.entry(CharSequence.class, raw -> raw),
        Map.entry(int.class, Integer::parseInt),
        Map.entry(Integer.class, Integer::valueOf),
        Map.entry(long.class, Long::parseLong),
        Map.entry(Long.class, Long::valueOf),
        Map.entry(short.class, Short::parseShort),
        Map.entry(Short.class, Short::valueOf),
        Map.entry(byte.class, Byte::parseByte),
        Map.entry(Byte.class, Byte::valueOf),
        Map.entry(double.class, Double::parseDouble),
        Map.entry(Double.class, Double::valueOf),
        Map.entry(float.class, Float::parseFloat),
        Map.entry(Float.class, Float::valueOf),
        Map.entry(boolean.class, Boolean::parseBoolean),
        Map.entry(Boolean.class, Boolean::valueOf),
        Map.entry(char.class, raw -> raw.charAt(0)),
        Map.entry(Character.class, raw -> raw.charAt(0)),
        Map.entry(BigDecimal.class, BigDecimal::new),
        Map.entry(BigInteger.class, BigInteger::new),
        Map.entry(LocalDate.class, LocalDate::parse),
        Map.entry(Object.class, raw -> raw)
    );

    /**
     * Тип.
     */
    private final Class<?> type;

    /**
     * Основной конструктор.
     * @param type Тип
     */
    public Convertible(final Class<?> type) {
        this.type = type;
    }

    /**
     * Умеет ли движок построить значение этого типа из строки.
     * @return Умеет ли движок построить значение этого типа из строки
     */
    public boolean yes() {
        return this.type.isEnum() || RULES.containsKey(this.type);
    }
}
