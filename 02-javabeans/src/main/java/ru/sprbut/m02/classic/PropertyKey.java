/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m02.classic;

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
     * @return Ключ конфигурации в имя свойства: {@code first-name} и {@code first_name} одинаково становятся {@code firstName}
     */
    public String camelCase() {
        if (this.raw.indexOf('-') < 0 && this.raw.indexOf('_') < 0) {
            return this.raw;
        }
        final String[] parts = this.raw.split("[-_]");
        final StringBuilder joined = new StringBuilder(parts[0]);
        for (int index = 1; index < parts.length; index++) {
            if (!parts[index].isEmpty()) {
                joined.append(new PropertyKey(parts[index]).capitalized());
            }
        }
        return joined.toString();
    }

    /**
     * Имя свойства из имени метода: {@code Name} → {@code name}, {@code URL} → {@code URL}.
     * @return Имя свойства из имени метода: {@code Name} → {@code name}, {@code URL} → {@code URL}
     */
    public String decapitalized() {
        if (this.raw.length() > 1
            && Character.isUpperCase(this.raw.charAt(0))
            && Character.isUpperCase(this.raw.charAt(1))) {
            return this.raw;
        }
        return Character.toLowerCase(this.raw.charAt(0)) + this.raw.substring(1);
    }

    /**
     * Имя метода из имени свойства: {@code name} → {@code Name}.
     * @return Имя метода из имени свойства: {@code name} → {@code Name}
     */
    public String capitalized() {
        return Character.toUpperCase(this.raw.charAt(0)) + this.raw.substring(1);
    }
}
