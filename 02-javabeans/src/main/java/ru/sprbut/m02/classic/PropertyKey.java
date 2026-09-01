/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

import java.util.regex.Pattern;

/**
 * Имя свойства в разных написаниях.
 *
 * <p>Правило {@code decapitalize} взято из {@code java.beans} и выглядит странно
 * ровно один раз: {@code URL} остаётся {@code URL}, а {@code Name} становится
 * {@code name}. Две заглавные подряд означают аббревиатуру, и трогать её нельзя —
 * иначе {@code getURL()} превратился бы в свойство {@code uRL}.</p>
 *
 * @since 1.0
 */
public final class PropertyKey {

    /**
     * Разделитель частей ключа конфигурации.
     */
    private static final Pattern SEPARATOR = Pattern.compile("[-_]");

    /**
     * Исходное значение.
     */
    private final String raw;

    /**
     * Основной конструктор.
     * @param raw Исходное значение
     */
    public PropertyKey(final String raw) {
        this.raw = raw;
    }

    /**
     * Ключ конфигурации в имя свойства: {@code first-name} и {@code first_name}
     * одинаково становятся {@code firstName}.
     * @return Имя свойства, собранное из частей ключа
     */
    public String camelCase() {
        final String name;
        if (this.raw.indexOf('-') < 0 && this.raw.indexOf('_') < 0) {
            name = this.raw;
        } else {
            final String[] parts = PropertyKey.SEPARATOR.split(this.raw);
            final StringBuilder joined = new StringBuilder(parts[0]);
            for (int index = 1; index < parts.length; index += 1) {
                if (!parts[index].isEmpty()) {
                    joined.append(new PropertyKey(parts[index]).capitalized());
                }
            }
            name = joined.toString();
        }
        return name;
    }

    /**
     * Имя свойства из имени метода: {@code Name} даёт {@code name},
     * а {@code URL} остаётся {@code URL}.
     * @return Имя свойства из имени метода
     */
    public String decapitalized() {
        final String name;
        if (this.raw.length() > 1
            && Character.isUpperCase(this.raw.charAt(0))
            && Character.isUpperCase(this.raw.charAt(1))) {
            name = this.raw;
        } else {
            name = String.format(
                "%s%s", Character.toLowerCase(this.raw.charAt(0)), this.raw.substring(1)
            );
        }
        return name;
    }

    /**
     * Имя метода из имени свойства: {@code name} даёт {@code Name}.
     * @return Имя метода из имени свойства
     */
    public String capitalized() {
        return Character.toUpperCase(this.raw.charAt(0)) + this.raw.substring(1);
    }
}
