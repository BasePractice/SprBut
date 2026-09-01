/*
 * SPDX-FileCopyrightText: Copyright (c) 2026 Учебные репозитории
 * SPDX-License-Identifier: MIT
 */
// @checkstyle MultiLineCommentCheck disable
// @checkstyle RegexpSingleline disable
package ru.sprbut.m12.extended;

import java.util.List;

/**
 * Итог аудита одного класса.
 * @param type         проверенный класс
 * @param styles       найденные способы внедрения
 * @param dependencies типы зависимостей
 * @param testable     можно ли собрать объект обычным {@code new}
 * @param immutable    все ли поля объявлены {@code final}
 * @param warnings     замечания, каждое одним предложением
 * @since 1.0
 */
public record Report(
    Class<?> type,
    List<Style> styles,
    List<String> dependencies,
    boolean testable,
    boolean immutable,
    List<String> warnings
) {

    /**
     * Значение {@code Report}.
     */
    public Report {
        styles = List.copyOf(styles);
        dependencies = List.copyOf(dependencies);
        warnings = List.copyOf(warnings);
    }

    /**
     * Нет ли замечаний вовсе.
     */
    public boolean clean() {
        return this.warnings.isEmpty();
    }

    /**
     * Основной способ внедрения — первый найденный.
     */
    public Style primary() {
        return this.styles.isEmpty() ? Style.NONE : this.styles.get(0);
    }
}
