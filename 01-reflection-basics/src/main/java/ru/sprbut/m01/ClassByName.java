/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m01;

/**
 * Третий способ получить {@code Class}: загрузка по строковому имени.
 *
 * <p>На этом стоит чтение конфигураций, где имя класса лежит в текстовом файле,
 * и весь механизм плагинов. Здесь же прячется главная проблема native image:
 * связь через строку не видна ни компилятору, ни сборщику образа
 * (см. [модуль 22](../../../../../../22-aot-native)).</p>
 *
 * @since 1.0
 */
public final class ClassByName {

    /**
     * Имя.
     */
    private final String name;

    /**
     * Основной конструктор.
     * @param name Имя
     */
    public ClassByName(final String name) {
        this.name = name;
    }

    /**
     * Загруженный класс.
     * @return Загруженный класс
     * @throws ClassNotFoundException Если классу неоткуда взяться в classpath
     */
    public Class<?> type() throws ClassNotFoundException {
        return Class.forName(this.name);
    }
}
