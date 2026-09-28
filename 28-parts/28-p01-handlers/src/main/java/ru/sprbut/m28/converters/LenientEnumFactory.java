/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.converters;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * Фабрика конвертеров строки во все перечисления сразу.
 *
 * <p>{@code Converter} подбирается по точной паре типов, и для каждого
 * перечисления пришлось бы писать свой. {@code ConverterFactory} отвечает
 * за целое семейство: пара задаётся как «строка — любой наследник
 * {@code Enum}», а конкретный конвертер фабрика собирает под тип
 * параметра, когда тот встретится.</p>
 *
 * <p>Встроенная фабрика делает то же самое, но ищет константу по точному
 * имени: {@code ?role=admin} она отвергнет. Эта приводит строку к верхнему
 * регистру. Сырой тип {@code Enum} в сигнатуре — не небрежность: так
 * объявлена и встроенная фабрика, иначе {@code Enum.valueOf} не
 * получить.</p>
 *
 * @since 1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class LenientEnumFactory implements ConverterFactory<String, Enum> {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public LenientEnumFactory() {
        // нечего инициализировать
    }

    @Override
    public <T extends Enum> Converter<String, T> getConverter(final @NonNull Class<T> type) {
        return source -> (T) Enum.valueOf(type, source.trim().toUpperCase(Locale.ROOT));
    }
}
