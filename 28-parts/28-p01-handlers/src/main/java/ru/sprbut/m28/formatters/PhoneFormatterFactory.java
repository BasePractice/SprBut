/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
package ru.sprbut.m28.formatters;

import java.util.Set;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import ru.sprbut.m28.dto.Phone;

/**
 * Фабрика форматтеров телефона по аннотации.
 *
 * <p>Обычный форматтер глобален: зарегистрированный для типа, он
 * разбирает этот тип везде одинаково. Фабрика срабатывает только на
 * параметре с {@link PhoneNumber} и собирает форматтер под атрибуты
 * именно этой аннотации. Параметр {@code Phone} без метки остаётся без
 * разбора, и запрос к нему закончится ошибкой.</p>
 *
 * <p>Автоконфигурация сама подхватывает бины {@code Converter},
 * {@code Formatter} и {@code ConverterFactory}, а фабрику по аннотации —
 * нет. Её регистрирует {@code HandlersConfig.addFormatters}.</p>
 *
 * @since 1.0
 */
public final class PhoneFormatterFactory implements AnnotationFormatterFactory<PhoneNumber> {

    /**
     * Открытый конструктор: экземпляр создаёт конфигурация.
     */
    public PhoneFormatterFactory() {
        // нечего инициализировать
    }

    @Override
    public Set<Class<?>> getFieldTypes() {
        return Set.of(Phone.class);
    }

    @Override
    public Printer<?> getPrinter(final PhoneNumber annotation, final Class<?> type) {
        return new PhoneFormatter(annotation.country());
    }

    @Override
    public Parser<?> getParser(final PhoneNumber annotation, final Class<?> type) {
        return new PhoneFormatter(annotation.country());
    }
}
