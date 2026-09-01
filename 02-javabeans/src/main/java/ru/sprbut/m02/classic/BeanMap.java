/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Бин, представленный картой, где свойству отвечает значение.
 *
 * <p>Порядок отсортирован, чтобы результат был предсказуемым: в отчёте
 * об эффективной конфигурации приложения случайный порядок свойств
 * делает вывод бесполезным для сравнения.</p>
 *
 * @since 1.0
 */
public final class BeanMap {

    /**
     * Объект.
     */
    private final Object bean;

    /**
     * Основной конструктор.
     * @param bean Объект
     */
    public BeanMap(final Object bean) {
        this.bean = bean;
    }

    /**
     * Значения всех читаемых свойств.
     * @return Значения всех читаемых свойств
     */
    public Map<String, Object> values() {
        final Map<String, Object> collected = new LinkedHashMap<>();
        final Introspected introspected = new Introspected(this.bean.getClass());
        for (final String name : introspected.names()) {
            final PropertyDescriptor described = introspected.descriptor(name).orElseThrow();
            if (described.getReadMethod() != null) {
                collected.put(name, new Invoked(described.getReadMethod(), this.bean).value());
            }
        }
        return Map.copyOf(collected);
    }

    /**
     * То же, но значениями-строками — годится, чтобы напечатать конфигурацию.
     * @return То же, но значениями-строками — годится, чтобы напечатать конфигурацию
     */
    public Map<String, String> text() {
        final Map<String, String> printed = new LinkedHashMap<>();
        this.values().forEach((key, value) -> printed.put(key, String.valueOf(value)));
        return Map.copyOf(printed);
    }
}
